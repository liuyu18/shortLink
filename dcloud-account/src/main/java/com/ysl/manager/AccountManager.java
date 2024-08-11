package com.ysl.manager;

import com.ysl.model.AccountDO;
import java.util.List;

public interface AccountManager {

    int insert(AccountDO accountDO);
    List<AccountDO> findByPhone(String phone);
    
}