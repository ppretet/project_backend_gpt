package com.example.gpt.Controller;


import com.example.gpt.DAO.Goods;
import com.example.gpt.DAO.Order;
import com.example.gpt.DAO.OrderReturn;
import com.example.gpt.Service.GoodsService;
import com.example.gpt.Service.OrderService;
import com.example.gpt.Service.UserService;
import com.example.gpt.utils.Result;
import com.example.gpt.utils.generateGwt;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/order")
public class OrderController {
    private final generateGwt jwt = new generateGwt();
    @Autowired
    private UserService userService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private GoodsService goodsService;
    @GetMapping("/getorder")
    public Result getorder(HttpServletRequest request){
        String token = request.getHeader("Authorization").replace("Bearer ", "");

        String username = jwt.getUsernameFromToken(token);
        Integer user_id = userService.findIdByUsername(username);
        List<Order> orderList = orderService.queryAllbyUserId(user_id);
        List<OrderReturn>data= new ArrayList<>();
        for (Order order:orderList){
            OrderReturn orderReturn = new OrderReturn();
            orderReturn.setOrder_id(order.getOrder_id());
            orderReturn.setCreate_time(order.getCreated_at());
            orderReturn.setPay_link(order.getPay_link());
            Goods goods = goodsService.getGoodsbyGoodsId(order.getGoods_id());
            orderReturn.setDoc_id(goods.getTitle());
            orderReturn.setPrice(goods.getPrice());
            orderReturn.setDoc_detail(goods.getDetail());
            data.add(orderReturn);
            //System.out.println(orderReturn);
        }
        return Result.success(data);
    }
}
