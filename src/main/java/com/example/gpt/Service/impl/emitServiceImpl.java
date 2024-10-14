package com.example.gpt.Service.impl;

import com.example.gpt.DAO.Detail_Ask;
import com.example.gpt.DAO.Message;
import com.example.gpt.DAO.Sessionsation;
import com.example.gpt.Service.DetailAskService;
import com.example.gpt.Service.SessionsationService;
import com.example.gpt.Service.emitService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import okio.BufferedSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static com.example.gpt.Controller.emitStream.getString;

@Service
public class emitServiceImpl implements emitService {
    @Autowired
    private DetailAskService detailAskService;
    @Autowired
    private SessionsationService sessionsationService;

    private String extractContent(JsonNode jsonNode, ObjectMapper mapper) {
        // 处理 JSON 内容提取
        return getString(jsonNode);
    }

    @Override
    public void HelpSendResponse(ObjectMapper mapper, ExecutorService executor, List<Message> question, String model, String key,
                                 SseEmitter emitter, OkHttpClient client, Detail_Ask detailAsk, Sessionsation sessionsation) {
        final StringBuilder allContent = new StringBuilder();
        emitter.onCompletion(() -> System.out.println("Emitter 完成。"));
        emitter.onTimeout(() -> {
            System.out.println("Emitter 超时。");
            emitter.complete();
        });
        emitter.onError(t -> {
            System.out.println("发生错误: " + t.getMessage());
            emitter.completeWithError(t);
        });
        executor.execute(() -> {
            try {
                String url = "https://api.gptgod.online/v1/chat/completions";
                Map<String, Object> jsonMap = Map.of(
                        "model", model,
                        "messages", question,
                        "stream", true
                );
                String json = mapper.writeValueAsString(jsonMap);

                Request httpRequest = new Request.Builder()
                        .url(url)
                        .post(RequestBody.create(json, MediaType.parse("application/json")))
                        .addHeader("Authorization", "Bearer " + key)
                        .build();

                try (Response response = client.newCall(httpRequest).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        try (BufferedSource source = response.body().source()) {
                            while (!source.exhausted()) {
                                String line = source.readUtf8Line();
                                if (line != null && line.startsWith("data: ")) {
                                    //System.out.println(line);
                                    String dataStr = line.substring(5);
                                    if (dataStr.equals(" [DONE]")) {
                                        // 完成时的逻辑
                                        detailAsk.setAsk(allContent.toString());
                                        sessionsation.setContent(allContent.toString());
                                        sessionsation.setCreated_at(new Timestamp(System.currentTimeMillis()));
                                        detailAskService.save(detailAsk);
                                        sessionsationService.save(sessionsation);
                                        emitter.complete();
                                        String doneMessage = "[DONE]";
                                        String encodedDone = Base64.getEncoder().encodeToString(doneMessage.getBytes(StandardCharsets.UTF_8));
                                        emitter.send(SseEmitter.event().data(encodedDone));
                                        emitter.complete();  // 完成流
                                        break;
                                    }
                                    try {
                                        JsonNode jsonNode = mapper.readTree(dataStr);
                                        String content = extractContent(jsonNode, mapper);
                                        if (content != null && !content.isEmpty()) {
                                            allContent.append(content);
                                            String encodedContent = Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
                                            try{
                                                emitter.send(SseEmitter.event().data(encodedContent));  // 发送编码后的内容
                                            }catch (Exception e){
                                                System.out.println("发送错误"+e.getMessage());
                                                emitter.complete();
                                            }

                                        }
                                    } catch (Exception e) {
                                        System.out.println("JSONNODE错误"+e.getMessage());
                                        emitter.completeWithError(e);
                                    }
                                }
                            }
                        }catch (Exception e){
                            System.out.println("source错误"+e.getMessage());
                            //e.printStackTrace();
                            emitter.completeWithError(e);
                        }
                    } else {
                        String errorMsg = "Response not successful";
                        String encodedError = Base64.getEncoder().encodeToString(errorMsg.getBytes(StandardCharsets.UTF_8));
                        emitter.send(SseEmitter.event().data(encodedError));
                    }
                } catch (Exception e) {
                    System.out.println("response错误"+e.getMessage());
                    emitter.completeWithError(e);
                }
            } catch (Exception e){
                System.out.println("总错误"+e.getMessage());
                emitter.completeWithError(e);
            }
        });
    }


    @Override
    public void HelpSendError(SseEmitter emitter, String msg) throws IOException {
        String encodedContent = Base64.getEncoder().encodeToString(msg.getBytes(StandardCharsets.UTF_8));
        emitter.send(SseEmitter.event().data(encodedContent));
        String end = "[DONE]";
        String encodedContents = Base64.getEncoder().encodeToString(end.getBytes(StandardCharsets.UTF_8));
        emitter.send(SseEmitter.event().data(encodedContents));
        emitter.complete();
    }
}

