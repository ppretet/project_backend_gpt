package com.example.gpt.DAO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdAndUsernameFromConversation {
    private Integer id;
    private String username;

}
