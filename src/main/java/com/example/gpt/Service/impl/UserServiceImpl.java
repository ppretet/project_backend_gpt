package com.example.gpt.Service.impl;

import com.example.gpt.DAO.User;
import com.example.gpt.Service.UserService;
import com.example.gpt.mapper.UserMapper;
import com.example.gpt.utils.EncodePassword;
import com.example.gpt.utils.Result;
import com.example.gpt.utils.generateGwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    private final generateGwt gwt = new generateGwt();
    @Override
    public Result save(User user) {
//        Map<String,String>np = new HashMap<>();
        String email = user.getEmail();
        String username = user.getUsername();
//        np.put("username",username);
//        np.put("score", String.valueOf(user.getScored_points()));
        generateGwt gwt = new generateGwt();
//        np.put("token", gwt.generateToken(username));
        if(userMapper.findByEmail(email) != null){
            return Result.error("邮箱已存在");
        }
        if(userMapper.findByUsername(username) != null){
            return Result.error("用户名已存在");
        }

        int n = userMapper.save(user);
        if (n > 0){
            return Result.success("注册成功");
        }else {
            return Result.error("注册失败");
        }
    }


    @Override
    public Result login(String username,String password) {
        Map<String,String>np = new HashMap<>();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        //System.out.println(timestamp);

        if(username == null || password == null){
            return Result.error("账号或密码为空");
        }
        if(userMapper.findByUsername(username) != null &&
                EncodePassword.matchesPassword(password,userMapper.findByUsername(username).getPassword())){
            User user = userMapper.findByUsername(username);
            user.setLast_login(timestamp);
            userMapper.update(user);
            np.put("token",gwt.generateToken(username));
            np.put("username",username);
            np.put("score", String.valueOf(userMapper.findByUsername(username).getScored_points()));
//            System.out.println(userMapper.findByUsername(username));

            return Result.success(np);
        }
        if (userMapper.findByEmail(username) != null &&
                EncodePassword.matchesPassword(password,userMapper.findByEmail(username).getPassword())){
            User user = userMapper.findByEmail(username);
            user.setLast_login(timestamp);
            np.put("token",gwt.generateToken(userMapper.findByEmail(username).getUsername()));
            np.put("username",userMapper.findByEmail(username).getUsername());
            np.put("score", String.valueOf(userMapper.findByEmail(username).getScored_points()));
            return Result.success(np);
        }

        return Result.error("登录失败,账号或密码错误");
    }

    @Override
    public Result loginByJwt(String token){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        if (gwt.validateToken(token)){
            String username = gwt.getUsernameFromToken(token);
            Map<String,String>np = new HashMap<>();
            User user = userMapper.findByUsername(username);
            user.setLast_login(timestamp);
            userMapper.update(user);
            np.put("username",username);
            np.put("score", String.valueOf(userMapper.findByUsername(username).getScored_points()));
            return Result.success(np);
        }
        return Result.error("自动登录失败");
    }

    @Override
    public int updateScoredPoints(BigDecimal scored_points, Integer user_id) {
        userMapper.updateScoredPoints(scored_points,user_id);
        return 0;
    }

    @Override
    public Result getUserInfo(Integer id) {
        User user = userMapper.findById(id);
        return Result.success(user);
    }

    @Override
    public boolean updateUserInfo(User user) {
        return userMapper.update(user) > 0;
    }

    @Override
    public Integer findIdByUsername(String username) {
        return userMapper.findIdByUsername(username);
    }

    @Override
    public Result updatePassword(Integer id, String oldPassword, String newPassword) {

        User user = userMapper.findById(id);
        if (user==null){
            return Result.error("用户不存在");
        }
        if (newPassword.equals(oldPassword)){
            return Result.error("与旧密码相同");
        }
        user.setPassword(newPassword);
        if (userMapper.update(user) > 0){
            return Result.success();
        }

        return Result.error("修改失败");
    }


    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public User findByEmail(String email) {
        return null;
    }


    @Override
    public User findById(Integer id) {
        return null;
    }

    @Override
    public Result getUserList() {
        return null;
    }
}
