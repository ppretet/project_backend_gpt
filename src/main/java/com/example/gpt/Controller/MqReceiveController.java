package com.example.gpt.Controller;

import com.example.gpt.DAO.Model;
import com.example.gpt.DAO.Order;
import com.example.gpt.Service.ModelService;
import com.example.gpt.Service.OrderService;
import com.example.gpt.mapper.OrderMapper;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component

public class MqReceiveController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ModelService modelService;
    @RabbitListener(queues = "delay_queue_order")
    @RabbitHandler
    public void process(String msg) {
        System.out.println("delay_queue_order: " + msg);
        Order order = orderService.findOrder(msg);
        if(order.getStatus().equals("pending")){
            order.setStatus("cancel");
            orderMapper.updataPayANDFinishTime(order);
        }
    }

    @RabbitListener(queues = "use_queue")
    @RabbitHandler
    public void process2(String msg) {
        System.out.println("use_queue: "+msg);
        Model model = modelService.getModelByName(msg);
        model.setUsed(model.getUsed()+1);
        modelService.updateUsed(model);
    }
}
