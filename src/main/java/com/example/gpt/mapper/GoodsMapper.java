package com.example.gpt.mapper;

import com.example.gpt.DAO.Goods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GoodsMapper {
    @Select("select * from `goods` order by price")
    List<Goods>getAll();
    @Select("select price from `goods` where goods_id=#{id}")
    Double getPrice(String id);
    @Select("select * from `goods` where goods_id=#{goods_id}")
    Goods getGoodsbyGoodsId(String goods_id);

}
