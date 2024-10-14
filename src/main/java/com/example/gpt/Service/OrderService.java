package com.example.gpt.Service;

import com.alipay.api.AlipayApiException;
import com.example.gpt.DAO.Order;
import com.example.gpt.utils.Result;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

public interface OrderService {
    Integer findByOrderId(String order_id);
    int save(Order order);
    Result pay(Order order, Double price) throws AlipayApiException, UnsupportedEncodingException;
    List<Order> queryAllbyUserId(Integer user_id);

    Order findOrder(String order_id);
    Map<String,Object>getDetailAndOrderIdByOrderId(String orderId);
    void updateStatus(Integer id);

}
