package com.example.gpt.mapper;

import com.example.gpt.DAO.ReturnRecory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface IndexMapper {
    @Select("select content,img_url,role,created_at from sessionStation where session_id=#{id} order by created_at")
    List<ReturnRecory>getReturnRecory(Integer id);

}
