package com.example.gpt.filters;

import com.example.gpt.utils.Result;
import com.example.gpt.utils.Truck;
import com.example.gpt.utils.generateGwt;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
@Component
public class JwtInterceptor implements HandlerInterceptor {
    private final generateGwt jwt = new generateGwt();

    @Autowired
    private Truck truck;

    public boolean verify(String token){
        return jwt.validateToken(token);
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //System.out.println("Request URL: " + request.getRequestURL());
        if (request.getMethod().equals("OPTIONS")){
            return true;
        }
        //System.out.println("拦截器");
        String tokenData = request.getHeader("Authorization");
        if (tokenData != null){

            String token = tokenData.replace("Bearer ", "");
//            return verify(token);
            if (verify(token)){
                String username = jwt.getUsernameFromToken(token);
                if (truck.is_valid(username)){
                    return true;
                }
                //System.out.println("接口限流");
                throw new RuntimeException("接口限流");
            }

            //throw new RuntimeException("token验证失败");
        }
        String token = request.getParameter("token");
        if (verify(token)){
            String username = jwt.getUsernameFromToken(token);
            if (truck.is_valid(username)){
                return true;
            }
            //System.out.println("接口限流");
            throw new RuntimeException("接口限流");
        }
        throw new RuntimeException("token验证失败");
    }

}
