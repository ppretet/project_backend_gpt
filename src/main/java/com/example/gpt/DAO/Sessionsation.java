package com.example.gpt.DAO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Sessionsation {
    private Integer id;
    private String role;
    private String content;
    private Timestamp created_at;
    private Integer session_id;
    private String img_url;
}
