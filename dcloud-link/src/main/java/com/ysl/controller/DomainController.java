package com.ysl.controller;

import com.ysl.service.DomainService;
import com.ysl.util.JsonData;
import com.ysl.vo.DomainVO;
import io.swagger.v3.core.util.Json;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/domain/v1")
@RequiredArgsConstructor
public class DomainController {


    private final DomainService domainService;

    @GetMapping("list")
    public JsonData listAll(){

        List<DomainVO> list = domainService.listAll();
        return JsonData.buildSuccess(list);

    }
}
