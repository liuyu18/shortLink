package com.ysl.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.ysl.manager.ProductOrderManager;
import com.ysl.mapper.ProductOrderMapper;
import com.ysl.model.ProductOrderDO;
import com.ysl.vo.ProductOrderVO;
import groovy.util.logging.Slf4j;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class ProductOrderManagerImpl implements ProductOrderManager {

    private final ProductOrderMapper productOrderMapper;

    @Override
    public int add(ProductOrderDO productOrderDO) {
        return productOrderMapper.insert(productOrderDO);
    }

    @Override
    public ProductOrderDO findByOutTradeNoAndAccountNo(String outTradeNo, Long accountNo) {
        ProductOrderDO productOrderDO = productOrderMapper.selectOne(
                new QueryWrapper<ProductOrderDO>()
                        .eq("out_trade_no", outTradeNo)
                        .eq("account_no", accountNo)
                        .eq("del", 0)
        );
        return productOrderDO;
    }

    @Override
    public int updateOrderPayState(String outTradeNo, Long accountNo, String newState, String oldState) {
        int rows = productOrderMapper.update(
                null,
                new UpdateWrapper<ProductOrderDO>()
                        .eq("out_trade_no", outTradeNo)
                        .eq("account_no", accountNo)
                        .eq("state", oldState)
                        .set("state", newState)

        );
        return rows;
    }

    @Override
    public Map<String, Object> page(int page, int size, Long accountNo, String state) {
        Page<ProductOrderDO> pageInfo = new Page<>(page, size);
        IPage<ProductOrderDO> orderDOIPage;
        if (StringUtils.isBlank(state)) {
            orderDOIPage = productOrderMapper.selectPage(
                    pageInfo,
                    new QueryWrapper<ProductOrderDO>()
                            .eq("account_no", accountNo)
                            .eq("del", 0));
        } else {
            orderDOIPage = productOrderMapper.selectPage(
                    pageInfo,
                    new QueryWrapper<ProductOrderDO>()
                            .eq("account_no", accountNo)
                            .eq("state", state)
                            .eq("del", 0)
            );
        }
        List<ProductOrderDO> orderDOIPageRecords = orderDOIPage.getRecords();
        List<ProductOrderVO> productOrderVOList = orderDOIPageRecords.stream().map(obj -> {
            ProductOrderVO productOrderVO = new ProductOrderVO();
            BeanUtils.copyProperties(obj, productOrderVO);
            return productOrderVO;
        }).collect(Collectors.toList());
        Map<String, Object> pageMap = new HashMap<>(3);
        pageMap.put("total_record", orderDOIPage.getTotal());
        pageMap.put("total_page", orderDOIPage.getPages());
        pageMap.put("current_data", productOrderVOList);
        return pageMap;
    }

    @Override
    public int del(Long productOrderId, Long accountNo) {
        int rows = productOrderMapper.update(
                null,
                new UpdateWrapper<ProductOrderDO>()
                        .eq("id", productOrderId)
                        .eq("account_no", accountNo)

                        .set("del", 1));

        return rows;
    }
}
