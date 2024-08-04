package com.ysl.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ysl.enums.BizCodeEnum;
import com.ysl.service.FileService;
import com.ysl.util.JsonData;

/**
 *
 * @author ryan
 * @since 2024-06-16
 */
@RestController
@RequestMapping("/api/v1/account")
public class AccountController {

    private FileService fileService;

    AccountController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("upload")
    public JsonData uploadUserImg(@RequestPart("file") MultipartFile file) {

        String result = fileService.uploadUserImg(file);

        return result != null ? JsonData.buildSuccess(result)
                : JsonData.buildResult(BizCodeEnum.FILE_UPLOAD_USER_IMG_FAIL);

    }
}
