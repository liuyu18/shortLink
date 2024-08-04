package com.ysl.service.impl;

import com.ysl.model.AccountDO;
import com.ysl.controller.request.AccountRegisterRequest;
import com.ysl.enums.AuthTypeEnum;
import com.ysl.enums.BizCodeEnum;
import com.ysl.enums.SendCodeEnum;
import com.ysl.manager.AccountManager;
import com.ysl.service.AccountService;
import com.ysl.service.NotifyService;
import com.ysl.util.CommonUtil;
import com.ysl.util.JsonData;
import com.ysl.util.LogUtil;


import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang3.StringUtils;
import groovy.util.logging.Slf4j;


import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 *
 * @author ryan
 * @since 2024-06-16
 */
@Service
@Slf4j
public class AccountServiceImpl implements AccountService {
    private NotifyService notifyService;
    private AccountManager accountManager;

    @Override
    public JsonData register(AccountRegisterRequest registerRequest) {
        boolean checkCode = false;
        if (StringUtils.isNotBlank(registerRequest.getPhone())) {
            checkCode = notifyService.checkCode(SendCodeEnum.USER_REGISTER, registerRequest.getPhone(),
                    registerRequest.getCode());

        }

        if (!checkCode) {
            return JsonData.buildResult(BizCodeEnum.CODE_ERROR);
        }

        AccountDO accountDO = new AccountDO();
        BeanUtils.copyProperties(registerRequest, accountDO);
        accountDO.setAuth(AuthTypeEnum.DEFAULT.name());
        accountDO.setSecret("$1$" + CommonUtil.getStringNumRandom(8));
        String cryptPwd = Md5Crypt.md5Crypt(registerRequest.getPwd().getBytes(), accountDO.getSecret());
        accountDO.setPwd(cryptPwd);
        int rows = accountManager.insert(accountDO);
        LogUtil.info("rows:{},注册成功:{}", rows, accountDO);
        userRegisterInitTask(accountDO);

        return JsonData.buildSuccess();
    }

    private void userRegisterInitTask(AccountDO accountDO) {

    }

}
