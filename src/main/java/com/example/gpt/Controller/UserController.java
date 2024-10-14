package com.example.gpt.Controller;

import com.example.gpt.DAO.LoginData;
import com.example.gpt.DAO.User;
import com.example.gpt.DAO.UserPostData;
import com.example.gpt.Service.UserService;
import com.example.gpt.utils.EncodePassword;
import com.example.gpt.utils.Result;

import com.example.gpt.utils.generateGwt;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*")
public class UserController {
    @Autowired
    private UserService userService;
    private generateGwt jwt = new generateGwt();


    @PostMapping("/register")
    public Result register(@NotNull @RequestHeader(HttpHeaders.CONTENT_TYPE) String contentType,
                           @RequestBody(required = false) UserPostData UserPostData){
        if (!contentType.equals("application/json")){
            return Result.error("Content-Type must be application/json");
        }
        if(UserPostData == null){
            return Result.error("请求体为空");
        }

        String username = UserPostData.getUsername();
        String password = UserPostData.getPassword();

        //加密密码
        String encodepassword = EncodePassword.encodePassword(password);

        String email = UserPostData.getEmail();
        Timestamp createTime = new Timestamp(System.currentTimeMillis());
        User user = new User();
        user.setPassword(encodepassword);
        user.setEmail(email);
        user.setCreated_at(createTime);
        user.setUsername(username);
        user.setLast_login(createTime);
        user.setRole("user");
        user.setScored_points(BigInteger.valueOf(0));
        return userService.save(user);
    }

    @PostMapping("/login")
    public Result login(@NotNull @RequestHeader(HttpHeaders.CONTENT_TYPE) String contentType,
                        @RequestBody(required = false)LoginData loginData){
        if (!contentType.equals("application/json")){
            return Result.error("Content-Type must be application/json");
        }
        if (loginData == null){
            return Result.error("请求体为空");
        }
        return userService.login(loginData.getUsername(),loginData.getPassword());

    }

    @PostMapping("/loginByJwt")
    public Result loginByJwt(@RequestHeader("Authorization") String authorizationHeader){
        String token = authorizationHeader.replace("Bearer ", "");

        return userService.loginByJwt(token);
    }

    @PostMapping("/getscore")
    public Result getscore(@RequestParam String token){
        String username = jwt.getUsernameFromToken(token);
        User user = userService.findByUsername(username);
        Map<String, Object> result = new HashMap<>();
        result.put("username",username);
        result.put("score",user.getScored_points());
        return Result.success(result);
        //return userService.getUserInfo(id);
    }
}
