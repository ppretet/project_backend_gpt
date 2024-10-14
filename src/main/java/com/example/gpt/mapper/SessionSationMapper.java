package com.example.gpt.mapper;

import com.example.gpt.DAO.Sessionsation;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SessionSationMapper {
    @Insert("insert into sessionStation(role,content,created_at,session_id,img_url) " +
            "values (#{role},#{content},#{created_at},#{session_id},#{img_url})")
    public int save(Sessionsation sessionsation);

}
