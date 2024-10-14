package com.example.gpt.Controller;


import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayConfig;
import com.alipay.api.AlipayConstants;
import com.alipay.api.internal.util.AlipaySignature;
import com.example.gpt.DAO.Order;
import com.example.gpt.Service.GoodsService;
import com.example.gpt.Service.OrderService;
import com.example.gpt.Service.UserService;
import com.example.gpt.utils.Result;
import com.example.gpt.utils.generateGwt;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import static com.alipay.api.AlipayConstants.CHARSET_UTF8;

@RestController
@CrossOrigin("*")
@RequestMapping("/api")
public class AirPayController {
    private static final String public_key= "public key";

    private final ReentrantLock lock = new ReentrantLock();
    private generateGwt jwt = new generateGwt();
    @Autowired
    private UserService userService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private GoodsService goodsService;



    @PostMapping("/generateOrder")
    public Result generateOrder(@RequestParam String token,@RequestParam String id) throws AlipayApiException, UnsupportedEncodingException {
        Order order = new Order();
        Double price = goodsService.getPrice(id);
        if (price==null){
            return Result.error("商品不存在");
        }
        String username = jwt.getUsernameFromToken(token);
        Integer user_id = userService.findIdByUsername(username);
        order.setUser_id(user_id);
        order.setStatus("pending");
        order.setGoods_id(id);
        long time =System.currentTimeMillis();
        order.setCreated_at(new Timestamp(time));
        orderService.save(order);
        return orderService.pay(order,price);
    }

    @PostMapping("/pay")
    public Result pay(@RequestParam String token,@RequestParam String id) throws AlipayApiException {


        return Result.success();

    }



    //支付宝支付后异步回调
    @PostMapping("/finish")
    public void finish(HttpServletRequest request, HttpServletResponse response)throws Exception{
        System.out.println("异步回调");

        //Map<String,String> parms = request.getParameterMap();
//        System.out.println(request.getParameterMap());
        PrintWriter out = response.getWriter();
        if(lock.tryLock()){
            try{
                request.setCharacterEncoding("utf-8");
                Map<String, String> params = new HashMap<>(8);
                Map<String, String[]> requestParams = request.getParameterMap();
                for (Map.Entry<String, String[]> stringEntry : requestParams.entrySet()) {
                    String[] values = stringEntry.getValue();
                    String valueStr = "";
                    for (int i = 0; i < values.length; i++) {
                        valueStr = (i == values.length - 1) ? valueStr + values[i]
                                : valueStr + values[i] + ",";
                    }
                    params.put(stringEntry.getKey(), valueStr);
                }
                boolean  signVerified = AlipaySignature.rsaCheckV1(params, public_key, CHARSET_UTF8, AlipayConstants.SIGN_TYPE_RSA2) ;
                if (signVerified){
                    System.out.println("成功获取订单数据");
                    String notifyId = new String(params.get("notify_id").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                    // 通知时间
                    String notifyTime = new String(params.get("notify_time").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                    //商户订单号,之前生成的带用户ID的订单号
                    String outTradeNo = new String(params.get("out_trade_no").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                    //支付宝交易号
                    String tradeNo = new String(params.get("trade_no").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                    //付款金额
                    String totalAmount = new String(params.get("total_amount").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                    //交易状态
                    String tradeStatus = new String(params.get("trade_status").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                    Map<String,Object> mp = orderService.getDetailAndOrderIdByOrderId(outTradeNo);
                    if ("TRADE_SUCCESS".equals(tradeStatus)){
                        Integer id = (Integer) mp.get("id");
                        String detail = (String) mp.get("detail");
                        Integer user_id= (Integer) mp.get("user_id");
                        BigDecimal bigIntegerValue = new BigDecimal(detail.replace("万积分", "").trim());
                        bigIntegerValue = bigIntegerValue.multiply(BigDecimal.valueOf(10000));
                        System.out.println("需要添加积分: "+bigIntegerValue);
                        userService.updateScoredPoints(bigIntegerValue,user_id);
                        orderService.updateStatus(id);
                    }
                }
                else{
                    out.print("fail");
                    return;
                }

            }finally {
                lock.unlock();
            }
        }
        out.print("success");
        //return "success";
    }

    @GetMapping("/getfinish")
    public void getfinish(HttpServletRequest request, HttpServletResponse response)throws Exception{
        System.out.println("同步回调");
        String url="redirect:" + "https://www.baidu.com/";
        response.sendRedirect(url);

    }
}
