package com.example.gpt.DAO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationOld {
    private String session_id;
    private Timestamp create_at;
    private String title;
}
