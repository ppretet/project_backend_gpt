package com.example.gpt.Controller;

import com.example.gpt.DAO.IdAndUsernameFromConversation;
import com.example.gpt.DAO.ReturnRecory;
import com.example.gpt.Service.ConversationService;
import com.example.gpt.Service.IndexService;
import com.example.gpt.utils.Result;
import com.example.gpt.utils.generateGwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
@CrossOrigin("*")
@RequestMapping("/index")
public class indexController {
    generateGwt generateGwt = new generateGwt();
    @Autowired
    private ConversationService conversationService;
    @Autowired
    private IndexService indexService;
    @PostMapping("/recovery")
    public Result recovery(@RequestParam String sessionId, @RequestBody Map<String,String>jsonData){
//        System.out.println(sessionId);
//        System.out.println(jsonData);
        String token = jsonData.get("token");
        String username = generateGwt.getUsernameFromToken(token);
        IdAndUsernameFromConversation idAndUsernameFromConversation = conversationService.getIdAndUsernameBySessionId(sessionId);
        if (!Objects.equals(username, idAndUsernameFromConversation.getUsername())){
            return Result.error("信息不匹配");
        }
        return Result.success(indexService.getOldSessionList(idAndUsernameFromConversation.getId()));
    }
}
