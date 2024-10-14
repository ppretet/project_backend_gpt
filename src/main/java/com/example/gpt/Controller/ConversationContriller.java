package com.example.gpt.Controller;

import com.example.gpt.Service.ConversationService;
import com.example.gpt.utils.Result;
import com.example.gpt.utils.generateGwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping("/session")
public class ConversationContriller {
    generateGwt jwt = new generateGwt();
    @Autowired
    private ConversationService conversationService;
    @GetMapping("/generateSessionId")
    public Result generateSessionId(@RequestParam String token){
        String username = jwt.getUsernameFromToken(token);
        return conversationService.generateSession(username);
    }

    @GetMapping("/getall")
    public Result getAll(@RequestParam String token){
        String username = jwt.getUsernameFromToken(token);
        return conversationService.getSessionList(username);
    }

    @DeleteMapping("/delete_session")
    public Result deleteSession(@RequestParam String token,@RequestParam String session_id){
        String username = jwt.getUsernameFromToken(token);
        return conversationService.deleteSession(username,session_id);
    }
}
