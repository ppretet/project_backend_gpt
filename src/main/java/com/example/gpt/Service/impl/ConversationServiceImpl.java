package com.example.gpt.Service.impl;

import com.example.gpt.DAO.Conversation;
import com.example.gpt.DAO.ConversationOld;
import com.example.gpt.DAO.IdAndUsernameFromConversation;
import com.example.gpt.DAO.User;
import com.example.gpt.Service.ConversationService;
import com.example.gpt.Service.UserService;
import com.example.gpt.mapper.ConversationMapper;
import com.example.gpt.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ConversationServiceImpl implements ConversationService {
    @Autowired
    UserService userService;
    @Autowired
    ConversationMapper conversationMapper;
    @Override
    public Result generateSession(String username) {
        User user = userService.findByUsername(username);
        if(user!=null){
            String session_id = UUID.randomUUID().toString().replace("-", "");;
            Integer id = user.getId();
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            Conversation conversation = new Conversation();
            conversation.setUser_id(id);
            conversation.setCreate_at(timestamp);
            conversation.set_deleted(false);
            conversation.setUsername(username);
            conversation.setSession_id(session_id);
            conversation.setTitle("创建的新会话");
            conversationMapper.save(conversation);
            Map<String,String> dp = new HashMap<>();
            dp.put("sessionId",session_id);
            return Result.success(dp);
        }
        return Result.error("生成session失败");
    }

    @Override
    public Result getSessionList(String username) {
        return Result.success(conversationMapper.getSessionList(username));
    }

    @Override
    public Result deleteSession(String username, String sessionId) {
           Conversation conversation = conversationMapper.getConversationBySessionId(sessionId);
           if(conversation==null){
               return Result.error("删除失败");
           }
           if(conversation.getUsername().equals(username)){
               conversation.set_deleted(true);
               int x = conversationMapper.deleteSession(conversation);
               if(x==0){
                   return Result.error("删除失败");
               }
               return Result.success();
           }
           return Result.error("删除失败");
    }

    @Override
    public void updataTitle(String title,String sessionId){
        conversationMapper.updateTitle(title,sessionId);
    }

    @Override
    public Integer getConversationId(String sessionId) {
        return conversationMapper.getIdBySessionId(sessionId);
    }

    @Override
    public String getUsernameBySessionId(String sessionId) {
        return conversationMapper.getUsernameBySessionId(sessionId);
    }

    @Override
    public IdAndUsernameFromConversation getIdAndUsernameBySessionId(String sessionId) {
        return conversationMapper.getIdAndUsernameBySessionId(sessionId);
    }

}
