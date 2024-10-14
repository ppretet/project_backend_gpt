package com.example.gpt.mapper;

import com.example.gpt.DAO.Detail_Ask;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DetailAskMapper {
    @Insert("insert into ask_detail(model,question,ask,created_at,user_id,session_id)" +
            "values (#{model},#{question},#{ask},#{created_at},#{user_id},#{session_id})")
    int save(Detail_Ask detail_ask);

}
