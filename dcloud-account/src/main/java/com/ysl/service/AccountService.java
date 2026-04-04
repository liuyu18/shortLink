package com.ysl.service;

import com.ysl.controller.request.AccountLoginRequest;
import com.ysl.controller.request.AccountRegisterRequest;
import com.ysl.util.JsonData;

/**
 *
 * @author ryan
 * @since 2024-06-16
 */
public interface AccountService {
     JsonData register(AccountRegisterRequest registerRequest);
     JsonData login(AccountLoginRequest request);
}
