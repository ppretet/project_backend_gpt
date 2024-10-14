package com.example.gpt.DAO;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserPostData implements Serializable {
    private String username;
    private String password;
    private String email;
}
