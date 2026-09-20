package com.ysl.service;

import com.ysl.vo.ProductVO;

import java.util.List;

public interface ProductService {
    List<ProductVO> list();

    ProductVO findDetailById(long productId);
}
