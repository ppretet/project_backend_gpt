package com.example.gpt.mapper;

import com.example.gpt.DAO.Model;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ModelMapper {

    @Select("select * from model")
    List<Model> getAllModel();
    @Select("select * from model where name=#{name}")
    Model getModelByName(String name);
    @Update("update model set used=#{used} where name=#{name}")
    int updateUsed(Model model);
}
