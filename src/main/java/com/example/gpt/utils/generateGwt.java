package com.example.gpt.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class generateGwt {
    private final String secretKey = "random123456nfy123456...200453";

    public String generateToken(String username){
        Map<String,Object> claims = new HashMap<>();


        return createToken(claims,username);
    }

    private String createToken(Map<String, Object> claims, String username) {
        //过期时间
        long expirationTime = 2592000000L;
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+ expirationTime))
                .signWith(SignatureAlgorithm.HS256,secretKey)
                .compact();
    }


    public boolean validateToken(String token){
        try {
            if(token==null){
                return false;
            }
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    public String getUsernameFromToken(String token){
        Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
        return claims.getSubject();
    }


}
