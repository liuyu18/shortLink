package com.ysl.controller;

import com.ysl.controller.request.LinkGroupAddRequest;
import com.ysl.controller.request.LinkGroupUpdateRequest;
import com.ysl.enums.BizCodeEnum;
import com.ysl.service.LinkGroupService;
import com.ysl.util.JsonData;
import com.ysl.vo.LinkGroupVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/group/v1")
@RequiredArgsConstructor
public class LinkGroupController {

    private final LinkGroupService linkGroupService;
    @PostMapping("/add")
    public JsonData add(@RequestBody LinkGroupAddRequest addRequest) {
        int rows = linkGroupService.add(addRequest);
        return rows == 1 ? JsonData.buildSuccess() : JsonData.buildError("添加失败");
    }
    @GetMapping("detail/{group_id}")
    public JsonData del(@PathVariable("group_id") Long id) {
        int rows = linkGroupService.delete(id);
        return rows == 1 ? JsonData.buildSuccess():JsonData.buildResult(BizCodeEnum.GROUP_NOT_EXIST);
    }

    @GetMapping("list")
    public JsonData findUserAllLinkGroup(){

        List<LinkGroupVO> list = linkGroupService.listAllGroup();

        return JsonData.buildSuccess(list);

    }

    @PutMapping("update")
    public JsonData update(@RequestBody LinkGroupUpdateRequest request){

        int rows = linkGroupService.updateById(request);
        return rows == 1 ? JsonData.buildSuccess():JsonData.buildResult(BizCodeEnum.GROUP_OPER_FAIL);

    }
}
