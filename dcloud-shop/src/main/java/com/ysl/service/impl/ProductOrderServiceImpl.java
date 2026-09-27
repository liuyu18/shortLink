package com.ysl.service.impl;

import com.ysl.constant.TimeConstant;
import com.ysl.controller.request.ConfirmOrderRequest;
import com.ysl.controller.request.ProductOrderPageRequest;
import com.ysl.enums.BillTypeEnum;
import com.ysl.enums.BizCodeEnum;
import com.ysl.enums.ProductOrderPayTypeEnum;
import com.ysl.enums.ProductOrderStateEnum;
import com.ysl.exception.BizException;
import com.ysl.interceptor.LoginInterceptor;
import com.ysl.manager.ProductManager;
import com.ysl.manager.ProductOrderManager;
import com.ysl.model.LoginUser;
import com.ysl.model.ProductDO;
import com.ysl.model.ProductOrderDO;
import com.ysl.service.ProductOrderService;
import com.ysl.util.CommonUtil;
import com.ysl.util.JsonData;
import com.ysl.util.JsonUtil;
import com.ysl.vo.PayInfoVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class ProductOrderServiceImpl implements ProductOrderService {


    private final ProductOrderManager productOrderManager;
    private final ProductManager productManager;

    @Override
    public Map<String, Object> page(ProductOrderPageRequest orderPageRequest) {
        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        Map<String, Object> pageResult = productOrderManager.page(orderPageRequest.getPage(), orderPageRequest.getSize(), accountNo, orderPageRequest.getState());
        return pageResult;
    }

    @Override
    public String queryProductOrderState(String outTradeNo) {

        Long accountNo = LoginInterceptor.threadLocal.get().getAccountNo();
        ProductOrderDO productOrderDO = productOrderManager.findByOutTradeNoAndAccountNo(
                outTradeNo, accountNo
        );
        if (productOrderDO == null) {
            return "";
        } else {
            return productOrderDO.getState();
        }

    }

    @Override
    @Transactional
    public JsonData confirmOrder(ConfirmOrderRequest orderRequest) {
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        String orderOutTradeNo = CommonUtil.getStringNumRandom(32);
        ProductDO productDO = productManager.findDetailById(orderRequest.getProductId());

        this.checkPrice(productDO, orderRequest);
        ProductOrderDO productOrderDO = this.saveProductOrder(orderRequest, loginUser, orderOutTradeNo, productDO);
        PayInfoVO payInfoVO = PayInfoVO.builder()
                .accountNo(loginUser.getAccountNo())
                .outTradeNo(orderOutTradeNo)
                .clientType(orderRequest.getClientType())
                .payType(orderRequest.getPayType())
                .title(productDO.getTitle()).description("")
                .payFee(orderRequest.getPayAmount())
                .orderPayTimeoutMills(TimeConstant.ORDER_PAY_TIMEOUT_MILLS)
                .build();
        return null;
    }


    private ProductOrderDO saveProductOrder(ConfirmOrderRequest orderRequest, LoginUser loginUser, String orderOutTradeNo, ProductDO productDO) {
        ProductOrderDO productOrderDO = new ProductOrderDO();

        //设置用户信息
        productOrderDO.setAccountNo(loginUser.getAccountNo());
        productOrderDO.setNickname(loginUser.getUsername());


        //设置商品信息
        productOrderDO.setProductId(productDO.getId());
        productOrderDO.setProductTitle(productDO.getTitle());
        productOrderDO.setProductSnapshot(JsonUtil.obj2Json(productDO));
        productOrderDO.setProductAmount(productDO.getAmount());

        //设置订单信息
        productOrderDO.setBuyNum(orderRequest.getBuyNum());
        productOrderDO.setOutTradeNo(orderOutTradeNo);
        productOrderDO.setCreateTime(new Date());
        productOrderDO.setDel(0);

        //发票信息
        productOrderDO.setBillType(BillTypeEnum.valueOf(orderRequest.getBillType()).name());
        productOrderDO.setBillHeader(orderRequest.getBillHeader());
        productOrderDO.setBillReceiverPhone(orderRequest.getBillReceiverPhone());
        productOrderDO.setBillReceiverEmail(orderRequest.getBillReceiverEmail());
        productOrderDO.setBillContent(orderRequest.getBillContent());


        //实际支付总价
        productOrderDO.setPayAmount(orderRequest.getPayAmount());
        //总价，没使用优惠券
        productOrderDO.setTotalAmount(orderRequest.getTotalAmount());
        //订单状态
        productOrderDO.setState(ProductOrderStateEnum.NEW.name());
        //支付类型
        productOrderDO.setPayType(ProductOrderPayTypeEnum.valueOf(orderRequest.getPayType()).name());

        //插入数据库
        productOrderManager.add(productOrderDO);

        return productOrderDO;
    }

    private void checkPrice(ProductDO productDO, ConfirmOrderRequest orderRequest) {
        BigDecimal bizTotal = BigDecimal.valueOf(orderRequest.getBuyNum()).multiply(productDO.getAmount());
        if (bizTotal.compareTo(orderRequest.getPayAmount()) != 0) {
            log.error("验证价格失败{}", orderRequest);
            throw new BizException(BizCodeEnum.ORDER_CONFIRM_PRICE_FAIL);
        }
    }

}
