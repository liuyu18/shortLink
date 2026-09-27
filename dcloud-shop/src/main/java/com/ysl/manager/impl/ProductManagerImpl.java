package com.ysl.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ysl.manager.ProductManager;
import com.ysl.mapper.ProductMapper;
import com.ysl.model.ProductDO;
import groovy.util.logging.Slf4j;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@AllArgsConstructor
public class ProductManagerImpl implements ProductManager {

    private final ProductMapper productMapper;

    @Override
    public List<ProductDO> list() {
        return productMapper.selectList(null);
    }

    @Override
    public ProductDO findDetailById(long productId) {
        return productMapper.selectOne(new QueryWrapper<ProductDO>().eq("id", productId));
    }
}
