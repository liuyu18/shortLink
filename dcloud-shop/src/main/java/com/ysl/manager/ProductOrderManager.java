package com.ysl.manager;

import com.ysl.model.ProductOrderDO;

import java.util.Map;

public interface ProductOrderManager {
    int add(ProductOrderDO productOrderDO);

    ProductOrderDO findByOutTradeNoAndAccountNo(String outTradeNo, Long accountNo);

    int updateOrderPayState(String outTradeNo, Long  accountNo, String newState, String oldState);

    Map<String,Object> page(int page,int size, Long accountNo,String state);

    int del(Long productOrderId, Long accountNo);
}
