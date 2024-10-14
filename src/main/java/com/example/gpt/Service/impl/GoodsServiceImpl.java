package com.example.gpt.Service.impl;

import com.example.gpt.DAO.Goods;
import com.example.gpt.Service.GoodsService;
import com.example.gpt.mapper.GoodsMapper;
import com.example.gpt.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    private GoodsMapper goodsMapper;
    @Override
    public List<Goods> getAllgoods() {
        return goodsMapper.getAll();
    }

    @Override
    public Double getPrice(String id) {
        return goodsMapper.getPrice(id);
    }

    @Override
    public Goods getGoodsbyGoodsId(String id) {
        return goodsMapper.getGoodsbyGoodsId(id);
    }
}
