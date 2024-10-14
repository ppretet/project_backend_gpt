package com.example.gpt.Service;

import com.example.gpt.DAO.User;
import com.example.gpt.utils.Result;

import java.math.BigDecimal;

public interface UserService {
    Result save(User user);
    Result login(String username,String password);
    Result getUserInfo(Integer id);
    boolean updateUserInfo(User user);
    Integer findIdByUsername(String username);
    Result updatePassword(Integer id,String oldPassword,String newPassword);
    User findByUsername(String username);
    User findByEmail(String email);
    User findById(Integer id);
    Result getUserList();
    Result loginByJwt(String token);
    int updateScoredPoints(BigDecimal scored_points, Integer user_id);
}
