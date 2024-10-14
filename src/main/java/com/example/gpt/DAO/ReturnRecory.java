package com.example.gpt.DAO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReturnRecory {
    private String content;
    private String img_url;
    private String role;
    private Timestamp created_at;
}
