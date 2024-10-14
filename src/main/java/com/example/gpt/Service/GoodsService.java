package com.example.gpt.Service;

import com.example.gpt.DAO.Goods;
import com.example.gpt.utils.Result;

import java.util.List;

public interface GoodsService {
    List<Goods> getAllgoods();
    Double getPrice(String id);
    Goods getGoodsbyGoodsId(String id);
}
