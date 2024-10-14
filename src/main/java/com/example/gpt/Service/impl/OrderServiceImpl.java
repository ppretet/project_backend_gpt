package com.example.gpt.Service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.diagnosis.DiagnosisUtils;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.example.gpt.DAO.Order;
import com.example.gpt.Service.OrderService;
import com.example.gpt.mapper.OrderMapper;
import com.example.gpt.utils.Result;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {
    //oybtwb5350@sandbox.com

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    private static final String private_key = "your private_key";
    private static final String public_key= "public_key";
    private static final String notifyUrl = "notifyUrl";
    private static final String getNotifyUrl="getNotifyUrl";
    private static final String app_id = "your app_id";
    private static final String serverUrl = "serverUrl";
    @Override
    public Integer findByOrderId(String order_id) {
        return orderMapper.findByOrderId(order_id);
    }
    private String generateOrderId(){
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
    public void savaOrder(Order order){
        String order_id = generateOrderId();
        order.setOrder_id(order_id);
        try {
            orderMapper.save(order);
        }
        catch (DataIntegrityViolationException e){
            System.out.println(e);
            savaOrder(order);
        }
        return;
    }

    public static String getUrlParameter(String url, String parameterName) {
        String[] params = url.split("&");
        for (String param : params) {
            if (param.startsWith(parameterName + "=")) {
                return param.split("=")[1];
            }
        }
        return null;
    }

    @Override
    public int save(Order order) {
        savaOrder(order);
        return 1;
    }

    @Override
    public Result pay(Order order, Double price) throws AlipayApiException, UnsupportedEncodingException {
        String order_id = order.getOrder_id();
        //System.out.println("order-id: "+order_id);
        AlipayTradePagePayResponse response = getAlipayTradePagePayResponse(price, order_id);
        if (response.isSuccess()) {
            String url = response.getBody();

            //获取订单结束时间
            String timestamp = getUrlParameter(url, "timestamp");
            assert timestamp != null;
            String decodedTimestamp = URLDecoder.decode(timestamp, StandardCharsets.UTF_8);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime orderCreateTime = LocalDateTime.parse(decodedTimestamp, formatter);

            // 加上10分钟
            LocalDateTime orderEndTime = orderCreateTime.plusMinutes(10);

            // 格式化为timestamp格式并返回
            String formattedEndTime = orderEndTime.format(formatter);

            order.setFinish_at(Timestamp.valueOf(formattedEndTime));
            order.setPay_link(url);

            //发送到延迟队列mq中,超时取消订单
            sendtoMQ("delay_queue_order",order.getOrder_id());

            orderMapper.updataPayANDFinishTime(order);

            Map<String, String>data = new HashMap<>();
            data.put("order_id",order.getOrder_id());
            data.put("goods_id",order.getGoods_id());
            data.put("pay_link",order.getPay_link());
            data.put("finish_at", String.valueOf(order.getFinish_at()));
            return Result.success(data);
        } else {
             String diagnosisUrl = DiagnosisUtils.getDiagnosisUrl(response);
//             System.out.println(diagnosisUrl);
             return Result.error(diagnosisUrl);
        }

    }

    @Override
    public List<Order> queryAllbyUserId(Integer user_id) {
        return orderMapper.queryAllbyUserId(user_id);
    }

    @Override
    public Order findOrder(String order_id) {
        return orderMapper.findOrderByOrderId(order_id);
    }

    @Override
    public Map<String, Object> getDetailAndOrderIdByOrderId(String orderId) {
        return orderMapper.getDetailAndOrderIdByOrderId(orderId);
    }

    @Override
    public void updateStatus(Integer id) {
        orderMapper.updateStatus(id);
    }

    private void sendtoMQ(String delayQueueOrder, String orderId) {
        System.out.println("send to MQ");
        rabbitTemplate.convertAndSend("delay_exchange", delayQueueOrder, orderId, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setHeader("x-delay", 600000);
                return message;
            }
        });
    }

    private AlipayTradePagePayResponse getAlipayTradePagePayResponse(Double price, String order_id) throws AlipayApiException {
        AlipayClient alipayClient = new DefaultAlipayClient(getAlipayConfig());
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setTimeoutExpress("10m");
        model.setSubject("购买积分");
        model.setOutTradeNo(order_id);
        model.setTotalAmount(price.toString());
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        request.setNotifyUrl(notifyUrl);
        request.setReturnUrl(getNotifyUrl);
        request.setBizModel(model);
        AlipayTradePagePayResponse response = alipayClient.pageExecute(request,"GET");
        return response;
    }

    private AlipayConfig getAlipayConfig() {
        AlipayConfig alipayConfig = new AlipayConfig();
        alipayConfig.setAlipayPublicKey(public_key);
        alipayConfig.setPrivateKey(private_key);
        alipayConfig.setAppId(app_id);
        alipayConfig.setFormat("json");
        alipayConfig.setServerUrl(serverUrl);
        alipayConfig.setCharset("UTF-8");
        alipayConfig.setSignType("RSA2");

        return alipayConfig;
    }
}
