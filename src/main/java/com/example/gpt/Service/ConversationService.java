package com.example.gpt.Service;

import com.example.gpt.DAO.IdAndUsernameFromConversation;
import com.example.gpt.utils.Result;

public interface ConversationService {
    Result generateSession(String username);
    Result getSessionList(String username);
    Result deleteSession(String username,String sessionId);
    void updataTitle(String title,String sessionId);
    Integer getConversationId(String sessionId);

    String getUsernameBySessionId(String sessionId);
    IdAndUsernameFromConversation getIdAndUsernameBySessionId(String sessionId);
}
