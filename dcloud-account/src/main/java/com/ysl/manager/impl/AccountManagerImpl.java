package com.ysl.manager.impl;

import com.ysl.manager.AccountManager;
import com.ysl.mapper.AccountMapper;
import com.ysl.model.AccountDO;

import groovy.util.logging.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AccountManagerImpl implements AccountManager {
    private AccountMapper accountMapper;

    AccountManagerImpl(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    @Override
    public int insert(AccountDO accountDO) {

        return accountMapper.insert(accountDO);

    }

}