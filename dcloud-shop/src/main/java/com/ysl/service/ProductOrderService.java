package com.ysl.service;

import com.ysl.controller.request.ConfirmOrderRequest;
import com.ysl.controller.request.ProductOrderPageRequest;
import com.ysl.util.JsonData;

import java.util.Map;

public interface ProductOrderService {
    Map<String, Object> page(ProductOrderPageRequest orderPageRequest);

    String queryProductOrderState(String outTradeNo);

    JsonData confirmOrder(ConfirmOrderRequest orderRequest);
}
