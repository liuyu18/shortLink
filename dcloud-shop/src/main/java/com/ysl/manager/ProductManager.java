package com.ysl.manager;

import com.ysl.model.ProductDO;

import java.util.List;

public interface ProductManager {
    List<ProductDO> list();

    ProductDO findDetailById(long productId);
}
