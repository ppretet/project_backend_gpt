package com.example.gpt.mapper;

import com.example.gpt.DAO.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface UserMapper {
    @Select("select * from user where username=#{username}")
    public User findByUsername(String username);
    @Select("select * from user where id=#{id}")
    public User findById(Integer id);
    @Select("select * from user where email=#{email}")
    public User findByEmail(String email);

    @Select("select id from user where username=#{username}")
    public Integer findIdByUsername(String username);
    @Insert("insert into user(username,password,email,role,scored_points,last_login,created_at) " +
            "values(#{username},#{password},#{email},#{role},#{scored_points},#{last_login},#{created_at})")
    public int save(User user);
    @Update("update user set username=#{username},password=#{password},email=#{email}," +
            "role=#{role},scored_points=#{scored_points},last_login=#{last_login} where id=#{id}")
    public int update(User user);
    @Update("update user set scored_points=scored_points+#{scored_points} where id=#{user_id}")
    public int updateScoredPoints(BigDecimal scored_points, Integer user_id);

}
