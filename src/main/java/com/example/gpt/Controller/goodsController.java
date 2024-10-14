package com.example.gpt.Controller;

import com.alibaba.fastjson.JSON;
import com.example.gpt.DAO.Goods;
import com.example.gpt.Service.GoodsService;
import com.example.gpt.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@CrossOrigin("*")
@RequestMapping("/goods")
public class goodsController {
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private RedisTemplate<String,String>redisTemplate;
    @GetMapping("/goodsdata")

    //采用redis缓存数据
    public Result getAllgoods(){
        if(Boolean.FALSE.equals(redisTemplate.hasKey("goodsdata"))){
            System.out.println("查询数据库goods");
            List<Goods> goods= goodsService.getAllgoods();
            String jsonGoods= JSON.toJSONString(goods);
            redisTemplate.opsForValue().set("goodsdata",jsonGoods);
            redisTemplate.expire("goodsdata",15, TimeUnit.MINUTES);
            return Result.success(goods);
        }
        System.out.println("查询缓存goods");
        String data = redisTemplate.opsForValue().get("goodsdata");
        return Result.success(JSON.parseArray(data,Goods.class));
    }
}
