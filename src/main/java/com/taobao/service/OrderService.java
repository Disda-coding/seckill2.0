package com.taobao.service;

import com.taobao.error.BusinessException;
import com.taobao.service.model.OrderModel;

public interface OrderService {
    OrderModel createOrder(Integer userId,Integer itemId,Integer amount) throws BusinessException;

}
