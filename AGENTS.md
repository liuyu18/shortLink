# AGENTS.md

## Build & Run

- **Build**: `mvn clean package -DskipTests`
- **Run individual module**: `mvn -pl <module> spring-boot:run`
- **Skip tests by default**: Tests are disabled in pom.xml (`<skipTests>true</skipTests>`)

## Module Structure

| Module | Description |
|--------|-------------|
| `dcloud-common` | Shared utilities, interceptors, exceptions, config |
| `dcloud-account` | Account service (registration, login, traffic) |
| `dcloud-gateway` | API Gateway |
| `dcloud-link` | Short link service |
| `dcloud-shop` | Shop service |
| `dcloud-app` | Main application |
| `dcloud-data` | Data layer |

## Tech Stack

- Java 11
- Spring Boot 2.5.5 + Spring Cloud 2020.0.4
- MyBatis Plus 3.4.0
- Redis (Redisson 3.10.1)
- Alibaba Cloud services (OSS, SMS)
- ShardingSphere for database sharding

## Notes

- No `application.yml` in repo - config likely external or in other location
- Uses Aliyun Maven mirror: `maven.aliyun.com`
- Services use `@EnableFeignClients`, `@EnableDiscoveryClient`
- Database mappers in `dcloud-account/src/main/resources/mapper/`