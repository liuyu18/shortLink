package com.ysl.service.impl;

import com.ysl.manager.ProductManager;
import com.ysl.mapper.ProductMapper;
import com.ysl.model.ProductDO;
import com.ysl.service.ProductService;
import com.ysl.vo.ProductVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductManager productManager;


    @Override
    public List<ProductVO> list() {
        List<ProductDO> list = productManager.list();
        List<ProductVO> collect = list.stream().map(obj -> beanProcess(obj)).collect(Collectors.toList());

        return collect;
    }

    @Override
    public ProductVO findDetailById(long productId) {
        ProductDO productDO = productManager.findDetailById(productId);
        ProductVO productVO = beanProcess(productDO);

        return productVO;
    }


    private ProductVO beanProcess(ProductDO productDO) {
        ProductVO productVO = new ProductVO();
        BeanUtils.copyProperties(productDO, productVO);
        return productVO;
    }
}
