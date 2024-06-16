package com.ysl.service.impl;

import com.ysl.model.AccountDO;
import com.ysl.mapper.AccountMapper;
import com.ysl.service.AccountService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 *
 * @author ryan
 * @since 2024-06-16
 */
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, AccountDO> implements AccountService {

}
