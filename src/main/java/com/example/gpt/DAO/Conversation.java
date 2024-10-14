package com.example.gpt.DAO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Conversation {
    private Integer id;
    private String session_id;
    private boolean is_deleted;
    private Timestamp create_at;
    private Integer user_id;
    private String username;
    private String title;
}
