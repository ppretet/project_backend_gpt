package com.example.gpt.Service;

import com.example.gpt.DAO.Detail_Ask;
import com.example.gpt.DAO.Message;
import com.example.gpt.DAO.Sessionsation;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public interface emitService {
    void HelpSendResponse(ObjectMapper mapper, ExecutorService executor, List<Message> question, String model, String key,
                          SseEmitter emitter, OkHttpClient client, Detail_Ask detailAsk, Sessionsation sessionsation);
    void HelpSendError(SseEmitter emitter,String msg) throws IOException;
}
