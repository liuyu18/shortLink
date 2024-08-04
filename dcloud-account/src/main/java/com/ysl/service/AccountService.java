package com.ysl.service;

import com.ysl.controller.request.AccountRegisterRequest;
import com.ysl.model.AccountDO;
import com.ysl.util.JsonData;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 *
 * @author ryan
 * @since 2024-06-16
 */
public interface AccountService {
     JsonData register(AccountRegisterRequest registerRequest);
}
