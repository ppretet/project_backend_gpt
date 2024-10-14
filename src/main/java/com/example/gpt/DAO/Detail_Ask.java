package com.example.gpt.DAO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Detail_Ask {
    private Integer id;
    private String model;
    private String question;
    private String ask;
    private Timestamp created_at;
    private Integer user_id;
    private Integer session_id;
}
