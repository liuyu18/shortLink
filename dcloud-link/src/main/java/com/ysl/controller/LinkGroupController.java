package com.ysl.controller;

import com.ysl.controller.request.LinkGroupAddRequest;
import com.ysl.controller.request.LinkGroupUpdateRequest;
import com.ysl.enums.BizCodeEnum;
import com.ysl.service.LinkGroupService;
import com.ysl.util.JsonData;
import com.ysl.vo.LinkGroupVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/group/v1")
@RequiredArgsConstructor
@Tag(name = "短链分组模块", description = "短链分组新增、删除、列表和更新接口")
public class LinkGroupController {

    private final LinkGroupService linkGroupService;
    @PostMapping("/add")
    @Operation(summary = "新增短链分组", description = "为当前登录账号新增一个短链分组。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "新增结果", content = @Content(schema = @Schema(implementation = JsonData.class)))
    })
    public JsonData add(
            @Parameter(description = "新增分组请求参数", required = true) @RequestBody LinkGroupAddRequest addRequest) {
        int rows = linkGroupService.add(addRequest);
        return rows == 1 ? JsonData.buildSuccess() : JsonData.buildError("添加失败");
    }
    @GetMapping("detail/{group_id}")
    @Operation(summary = "删除短链分组", description = "按分组 ID 删除当前登录账号下的短链分组。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "删除结果", content = @Content(schema = @Schema(implementation = JsonData.class)))
    })
    public JsonData del(
            @Parameter(description = "分组 ID", required = true, example = "1") @PathVariable("group_id") Long id) {
        int rows = linkGroupService.delete(id);
        return rows == 1 ? JsonData.buildSuccess():JsonData.buildResult(BizCodeEnum.GROUP_NOT_EXIST);
    }

    @GetMapping("list")
    @Operation(summary = "查询短链分组列表", description = "查询当前登录账号下的全部短链分组。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "分组列表，data 为 LinkGroupVO 数组", content = @Content(schema = @Schema(implementation = JsonData.class)))
    })
    public JsonData findUserAllLinkGroup(){

        List<LinkGroupVO> list = linkGroupService.listAllGroup();

        return JsonData.buildSuccess(list);

    }

    @PutMapping("update")
    @Operation(summary = "更新短链分组", description = "按分组 ID 更新当前登录账号下的短链分组名称。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "更新结果", content = @Content(schema = @Schema(implementation = JsonData.class)))
    })
    public JsonData update(
            @Parameter(description = "更新分组请求参数", required = true) @RequestBody LinkGroupUpdateRequest request){

        int rows = linkGroupService.updateById(request);
        return rows == 1 ? JsonData.buildSuccess():JsonData.buildResult(BizCodeEnum.GROUP_OPER_FAIL);

    }
}
