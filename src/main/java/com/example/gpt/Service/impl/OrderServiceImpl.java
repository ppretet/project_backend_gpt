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
    private static final String private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC697WWSgOEkaBOO90r51tgar9XOcJmtBhGxSbcaNGieYH8t70uBqPxZo2S48L+ReFF+Y/MYjOEHNFniB7/0I/Dh9fqZRbOdG4s3oYJGg2RwwHiIUUkgDyJ//h9OfRTSA1Mwm2Rb7RI7t4EnRLCNIdzINvU/ctMZmf5E9YK7WN4LWbZdaeuH1zPCts4TNXNRous5wLsst9jkTJBel0ZB1YodxwVOZhSHIh9IHyqKf6TzIG5kz2anF2qzAQ3fgdGq6kXMJD+afYacIh5OfYBRPUrfqsA+PpSS9isNhnDgZ0JtFwZ0JGGhNz1JkAvZPgGHqmHglebdPC+DndRe9irOlW7AgMBAAECggEANk6VNB4zf2I9Sfwhdgsxg4P+J/vi96oAEy5Y51Sa77KTizBjhubu38OGA9CcZvctRAIFPryXchxcxtHHD08pMwZWNQM/6J/xEJ9iF2q13kziUyiOyNdmvj7Xkqguy7H2CwG+iawEldqsKnLla/uODGbS8770OjDUqoiUd4AUkGdAbAyF0Wc6HTKUcno21fKw3lJ2I4JPeBKRooZIommTBDK/rMA2V8AqT+0f3lm3l9II7lr4MvY3RJd7niAyC3juJHm63eKYpQoEWVJ83pyXNzJezhhy5OKqRweWjHekgRunxNFvytxAUFB0oF/sS0dt0U6JDNYqVyVIz+RYd3uEQQKBgQDkuXhfKXdEo8hXExkNXUjiBEnxAsg4lkemfITHuYBcAzIJwDX8e/7KEbskKbUVkJJafy/adTXyT8gGp1WWaqQ/hzdXsVekiCIxlTtrj5l9/tgzXH1fXmZoEc0vahmkFjZ9wZNXERDAE/r4r1dEj8oGZAg0ouW+qlpcoIcr5J4V+QKBgQDRQ3rdSICQLM98K3XSp+o0pUmTzABzjyESIQ2/bgWIS9DF8/kvY/znCCfdACO18UrRo3JqOISRMBsQlYouG2kh1nGe+YL7BhYNr/4G9ahf8FPCyOkdOJGWjt+B4gKq4z59Invjho8gvrs3WegyzPDWO1s6U1GBnnCz0tRk9vhmUwKBgFxGl1GNIRkIGqNGiSzjHaxRnqMlRDHQL0fzpH4PUMHOaDmW2jDqtXhAfwev6+avVNw9w8iW0RTZKxHEILt2ep1GqAUw/vLaGTF3y2kxnfM/BWpzPgFjzFzgEa+3VnDkpUdG+XJ+6AsWBIKf+s0mVZMOeGED9zxc7efAu8fxi/XBAoGBAKELkY6isW5DThp9R9uqpi+F1K4NnXtRd14AWhgsj81oq27sZ6T1CjiyqhsGdHUdETts0iy0+jDSGMIWk5UQO0RzSFXUJ90PvgEipBivDblnts582ApI8qIvqgrG6M6ivACJfVaIw7Wrk2DxrCUMW3xDid3FKUDbslhxtEtPVbNJAoGAQcxPglnNH9Ste+RI1nLh/4LC/Mm+rBUkD6L2eNb7lh1BvDfto00GdBm4iUVwa+gQFsYDo+fv9O+pUMkk98n9tRlchETXjhkTz6j8h1xvOrEj/hjsBj9QXUnCIhgMH9j72pNaH1bHb9xYCNkbAJkvaxMcHupFWKohuK5GdjSzTm0=";
    private static final String public_key= "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAheKWt6TgBk5sdecvCkDBnZNNgklCmEpfi0TRo3gwM8dTSE7qecoQYyM2CfIPtpWsL5IQ6mgnYoki2Logv9L/rlqAOZqGxk7Cx6EtQcPS19Wv9OoH0PQlwF2/Fp8RsqsHcLIhRZZaHKpe6LwwD2j5C0Hyr0vTkZ39LEde2sv4LVqHk/CggM6KRkZodUa6Vgcb8P68hBWH/Ebi8xmlIow+qyR4v6w/Drd4pdyw7lbX8v3H29UlmPFOwHlOUQ69EA+cpx5710A3ksXH5imJRh7Uw26okGzxFFGvQv33TQzieJ2iMGvO6CwPy8zMht/GB7OrgSvIgtGAhIF9vV/M8SSLxQIDAQAB";
    private static final String notifyUrl = "http://w4afqr.natappfree.cc/api/finish";
    private static final String getNotifyUrl="http://w4afqr.natappfree.cc/api/getfinish";
    private static final String app_id = "9021000124688810";
    private static final String serverUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
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
