package com.ysl.controller;

import com.ysl.service.DomainService;
import com.ysl.util.JsonData;
import com.ysl.vo.DomainVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/domain/v1")
@RequiredArgsConstructor
@Tag(name = "域名模块", description = "短链域名查询接口")
public class DomainController {


    private final DomainService domainService;

    @GetMapping("list")
    @Operation(summary = "查询域名列表", description = "查询当前登录账号可使用的短链域名列表。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "域名列表，data 为 DomainVO 数组", content = @Content(schema = @Schema(implementation = JsonData.class)))
    })
    public JsonData listAll(){

        List<DomainVO> list = domainService.listAll();
        return JsonData.buildSuccess(list);

    }
}
