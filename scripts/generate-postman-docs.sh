#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUTPUT_DIR="${OUTPUT_DIR:-$ROOT_DIR/docs/postman}"
OPENAPI_GROUP="${OPENAPI_GROUP:-short-link}"
OPENAPI_TIMEOUT="${OPENAPI_TIMEOUT:-10}"

usage() {
  cat <<'EOF'
Usage:
  scripts/generate-postman-docs.sh [all|account|link]

Description:
  Exports the latest springdoc OpenAPI JSON from running local services.
  Also generates Postman Collection JSON files for direct Postman import.

Defaults:
  all services: account link
  output dir:   docs/postman
  group:        short-link

Environment variables:
  OUTPUT_DIR=/path/to/output       Override output directory
  OPENAPI_GROUP=short-link         Override springdoc group name
  OPENAPI_TIMEOUT=10               curl timeout seconds
  BASE_URL_ACCOUNT=http://...      Override account base URL
  BASE_URL_LINK=http://...         Override link base URL
  USE_NPX=1                        Try npx openapi-to-postmanv2 before jq fallback

Examples:
  scripts/generate-postman-docs.sh
  scripts/generate-postman-docs.sh account
  BASE_URL_ACCOUNT=http://127.0.0.1:8001 scripts/generate-postman-docs.sh account
  USE_NPX=1 scripts/generate-postman-docs.sh all
EOF
}

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

service_config() {
  case "$1" in
    account)
      SERVICE_NAME="dcloud-account"
      DEFAULT_BASE_URL="http://localhost:8001"
      ;;
    link)
      SERVICE_NAME="dcloud-link"
      DEFAULT_BASE_URL="http://localhost:8002"
      ;;
    *)
      echo "Unknown service: $1" >&2
      echo "Supported services: account link" >&2
      exit 1
      ;;
  esac
}

service_base_url() {
  service_key="$1"
  upper_key="$(printf '%s' "$service_key" | tr '[:lower:]-' '[:upper:]_')"
  env_key="BASE_URL_${upper_key}"
  eval "custom_base_url=\${$env_key:-}"

  if [ -n "$custom_base_url" ]; then
    printf '%s' "$custom_base_url"
  else
    printf '%s' "$DEFAULT_BASE_URL"
  fi
}

fetch_openapi() {
  service_key="$1"
  service_config "$service_key"

  base_url="$(service_base_url "$service_key")"
  openapi_file="$OUTPUT_DIR/${SERVICE_NAME}.openapi.json"
  tmp_file="$openapi_file.tmp"
  grouped_url="$base_url/v3/api-docs/$OPENAPI_GROUP"
  default_url="$base_url/v3/api-docs"

  echo "Generating OpenAPI: $SERVICE_NAME ($base_url)"

  if curl -fsS --max-time "$OPENAPI_TIMEOUT" "$grouped_url" -o "$tmp_file"; then
    source_url="$grouped_url"
  elif curl -fsS --max-time "$OPENAPI_TIMEOUT" "$default_url" -o "$tmp_file"; then
    source_url="$default_url"
  else
    rm -f "$tmp_file"
    echo "Warning: cannot fetch OpenAPI from $SERVICE_NAME. Is the service running?" >&2
    echo "Tried:" >&2
    echo "  $grouped_url" >&2
    echo "  $default_url" >&2
    return 1
  fi

  if ! jq empty "$tmp_file" >/dev/null; then
    rm -f "$tmp_file"
    echo "Warning: invalid JSON returned by $source_url" >&2
    return 1
  fi

  jq --arg baseUrl "$base_url" '.servers = [{"url": $baseUrl}]' "$tmp_file" > "$openapi_file"
  rm -f "$tmp_file"
  echo "  OpenAPI JSON: $openapi_file"

  convert_to_postman "$openapi_file" "$OUTPUT_DIR/${SERVICE_NAME}.postman_collection.json"
}

convert_to_postman() {
  openapi_file="$1"
  collection_file="$2"

  if command -v openapi2postmanv2 >/dev/null 2>&1; then
    if openapi2postmanv2 -s "$openapi_file" -o "$collection_file" -p -O folderStrategy=Tags >/dev/null; then
      echo "  Postman Collection: $collection_file"
      return
    else
      echo "  Warning: openapi2postmanv2 failed for $openapi_file" >&2
    fi
  fi

  if [ "${USE_NPX:-0}" = "1" ]; then
    if ! command -v npx >/dev/null 2>&1; then
      echo "  Warning: USE_NPX=1 but npx is not installed" >&2
    elif npx --yes openapi-to-postmanv2 -s "$openapi_file" -o "$collection_file" -p -O folderStrategy=Tags >/dev/null; then
      echo "  Postman Collection: $collection_file"
      return
    else
      echo "  Warning: npx openapi-to-postmanv2 failed for $openapi_file" >&2
    fi
  fi

  generate_basic_postman_collection "$openapi_file" "$collection_file"
}

generate_basic_postman_collection() {
  openapi_file="$1"
  collection_file="$2"
  collection_tmp="$collection_file.tmp"

  if jq --arg openapiGroup "$OPENAPI_GROUP" '
    . as $root |

    def server_url:
      ($root.servers[0].url // "http://localhost");

    def resolve_schema($schema):
      if ($schema["$ref"]? // "") != "" then
        $root | getpath(($schema["$ref"] | sub("^#/"; "") | split("/")))
      else
        $schema
      end;

    def sample_from_schema($schema):
      (resolve_schema($schema // {})) as $schema |
      if ($schema.example? // null) != null then
        $schema.example
      elif ($schema.default? // null) != null then
        $schema.default
      elif (($schema.type? // "") == "object") or (($schema.properties? // null) != null) then
        ($schema.properties // {} | with_entries(.value = sample_from_schema(.value)))
      elif ($schema.type? // "") == "array" then
        [sample_from_schema($schema.items // {})]
      elif (($schema.type? // "") == "integer") or (($schema.type? // "") == "number") then
        0
      elif ($schema.type? // "") == "boolean" then
        false
      else
        ""
      end;

    def param_value($param):
      if ($param.example? // null) != null then
        ($param.example | tostring)
      elif ($param.schema.example? // null) != null then
        ($param.schema.example | tostring)
      elif ($param.schema.default? // null) != null then
        ($param.schema.default | tostring)
      else
        ""
      end;

    def postman_path($path):
      "/" + ($path | sub("^/"; "") | split("/") | map(if test("^\\{.+\\}$") then ":" + (sub("^\\{"; "") | sub("\\}$"; "")) else . end) | join("/"));

    def path_segments($path):
      ($path | sub("^/"; "") | split("/") | map(if test("^\\{.+\\}$") then ":" + (sub("^\\{"; "") | sub("\\}$"; "")) else . end));

    def query_params($operation):
      [($operation.parameters // [])[]
        | select(.in == "query")
        | {
            key: .name,
            value: param_value(.),
            disabled: ((.required // false) | not),
            description: (.description // "")
          }
      ];

    def header_params($operation):
      [($operation.parameters // [])[]
        | select(.in == "header")
        | {
            key: .name,
            value: param_value(.),
            disabled: ((.required // false) | not),
            description: (.description // "")
          }
      ] +
      (if ($operation.requestBody.content["application/json"]? // null) != null then
        [{key: "Content-Type", value: "application/json"}]
      else
        []
      end);

    def path_variables($path; $operation):
      [($operation.parameters // [])[]
        | select(.in == "path")
        | {
            key: .name,
            value: param_value(.),
            description: (.description // "")
          }
      ];

    def request_body($operation):
      ($operation.requestBody.content["application/json"]? // null) as $json |
      if $json == null then
        null
      else
        {
          mode: "raw",
          raw: ((if ($json.example? // null) != null then $json.example else sample_from_schema($json.schema // {}) end) | tojson),
          options: {raw: {language: "json"}}
        }
      end;

    def url_object($path; $operation):
      query_params($operation) as $query |
      path_variables($path; $operation) as $variables |
      {
        raw: ("{{baseUrl}}" + postman_path($path) + (if ($query | length) > 0 then "?" + ($query | map(.key + "=" + .value) | join("&")) else "" end)),
        host: ["{{baseUrl}}"],
        path: path_segments($path)
      }
      + (if ($query | length) > 0 then {query: $query} else {} end)
      + (if ($variables | length) > 0 then {variable: $variables} else {} end);

    def request_item($path; $method; $operation):
      request_body($operation) as $body |
      {
        name: ($operation.summary // $operation.operationId // (($method | ascii_upcase) + " " + $path)),
        request: ({
          method: ($method | ascii_upcase),
          header: header_params($operation),
          url: url_object($path; $operation),
          description: ($operation.description // $operation.summary // "")
        } + (if $body == null then {} else {body: $body} end)),
        response: []
      };

    [
      $root.paths
      | to_entries[] as $path_entry
      | $path_entry.value
      | to_entries[]
      | select(.key as $method | ["get", "post", "put", "delete", "patch", "head", "options"] | index($method))
      | {
          tag: (.value.tags[0] // "Default"),
          item: request_item($path_entry.key; .key; .value)
        }
    ] as $operations |
    {
      info: {
        name: (($root.info.title // "OpenAPI") + " Postman Collection"),
        description: ($root.info.description // "Generated from springdoc OpenAPI JSON."),
        schema: "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
      },
      variable: [
        {key: "baseUrl", value: server_url}
      ],
      item: ([
        {
          name: "OpenAPI",
          item: [
            {
              name: "Get OpenAPI Docs",
              request: {
                method: "GET",
                header: [],
                url: {raw: "{{baseUrl}}/v3/api-docs", host: ["{{baseUrl}}"], path: ["v3", "api-docs"]},
                description: "Returns the default OpenAPI JSON."
              },
              response: []
            },
            {
              name: "Get Grouped OpenAPI Docs",
              request: {
                method: "GET",
                header: [],
                url: {raw: ("{{baseUrl}}/v3/api-docs/" + $openapiGroup), host: ["{{baseUrl}}"], path: ["v3", "api-docs", $openapiGroup]},
                description: "Returns the grouped OpenAPI JSON."
              },
              response: []
            }
          ]
        }
      ] + ($operations | sort_by(.tag) | group_by(.tag) | map({name: .[0].tag, item: map(.item)})))
    }
  ' "$openapi_file" > "$collection_tmp"; then
    mv "$collection_tmp" "$collection_file"
  else
    rm -f "$collection_tmp"
    echo "  Warning: built-in jq conversion failed for $openapi_file" >&2
    return 1
  fi

  echo "  Postman Collection: $collection_file"
  echo "  Note: generated by built-in jq fallback. Install openapi-to-postmanv2 for richer conversion."
}

main() {
  if [ "${1:-}" = "-h" ] || [ "${1:-}" = "--help" ]; then
    usage
    exit 0
  fi

  require_cmd curl
  require_cmd jq
  mkdir -p "$OUTPUT_DIR"

  if [ "$#" -eq 0 ] || [ "${1:-}" = "all" ]; then
    services="account link"
  else
    services="$*"
  fi

  generated_count=0
  for service_key in $services; do
    if fetch_openapi "$service_key"; then
      generated_count=$((generated_count + 1))
    fi
  done

  if [ "$generated_count" -eq 0 ]; then
    echo "No API docs were generated." >&2
    exit 1
  fi

  echo "Done. Generated docs are in: $OUTPUT_DIR"
}

main "$@"
