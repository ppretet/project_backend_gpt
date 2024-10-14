package com.example.gpt.Controller;
import com.example.gpt.DAO.*;
import com.example.gpt.Service.*;
import com.example.gpt.utils.Result;
import com.example.gpt.utils.generateGwt;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSource;
import okio.Buffer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import okhttp3.ResponseBody;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/emit")
public class emitStream {
    private final OkHttpClient client = new OkHttpClient();
    private final String key ="your key";
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    @Autowired
    private emitService emitService;
    @Autowired
    private UserService userService;
    @Autowired
    private ModelService modelService;
    @Autowired
    private ConversationService conversationService;
    @Autowired
    private DetailAskService detailAskService;
    @Autowired
    private SessionsationService sessionsationService;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    private static final Logger logger = LoggerFactory.getLogger(emitStream.class);
    private final generateGwt gwt = new generateGwt();

    @GetMapping("/verify")
    public Result verify(@RequestParam String token) {
        return Result.success();
    }

    public boolean ver(String token){
        return gwt.validateToken(token);
    }

    @PostMapping(value = "/sse-stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sseStream(@RequestParam String token,
    @RequestBody DataPromt DataPromt,@RequestParam String sessionId) throws IOException {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        String forwardedFor = request.getHeader("X-Forwarded-For");
        String forwardedHost = request.getHeader("X-Forwarded-Host");
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        if (forwardedFor != null || forwardedHost != null || forwardedProto != null) {
            // 处理代理请求
            // 例如：返回错误信息
            SseEmitter emitter = new SseEmitter(0L);
            emitService.HelpSendError(emitter, "代理请求不被允许");
            return emitter;
        }
        String model = DataPromt.getModel();
        SseEmitter emitter = new SseEmitter(-1L);
        emitter.onError(e->{
            System.out.println("error: "+e);
        });
        Detail_Ask detailAsk = new Detail_Ask();
        Sessionsation sessionsation = new Sessionsation();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        if (!ver(token)){
            emitService.HelpSendError(emitter,"请求失败,请重新登录");
            return emitter;
        }

        ObjectMapper mapper = new ObjectMapper();
//        List<Map<String, String>> question = mapper.readValue(questionJson, new TypeReference<List<Map<String, String>>>() {
//        });
//        System.out.println(DataPromt);
//        System.out.println(DataPromt.getMessage().size());
//        String model = DataPromt.getModel();
        List<Message> question = DataPromt.getMessage();


        String username = gwt.getUsernameFromToken(token);

        User user = userService.findByUsername(username);
        Model needmodel = modelService.getModelByName(model);
        if(user == null){
            emitService.HelpSendError(emitter,"请求失败,请重新登录");
            return emitter;
        }

        Timestamp created_at = new Timestamp(System.currentTimeMillis());
        Integer id = conversationService.getConversationId(sessionId);
        String context = question.get(question.size()-1).getContent();
        detailAsk.setUser_id(user.getId());
        detailAsk.setQuestion(context);
        detailAsk.setSession_id(id);
        detailAsk.setModel(model);
        detailAsk.setCreated_at(created_at);

        sessionsation.setRole("user");
        sessionsation.setCreated_at(created_at);
        sessionsation.setSession_id(id);
        sessionsation.setContent(context);
        sessionsationService.save(sessionsation);

        Sessionsation sessionsation2 = new Sessionsation();
        sessionsation2.setRole("assistant");
        sessionsation2.setSession_id(id);
        BigInteger score = user.getScored_points();
        if(needmodel == null){
            emitService.HelpSendError(emitter,"未找到该模型");
            return emitter;
        }
        Integer price = needmodel.getPrice();

        if(score.compareTo(BigInteger.valueOf(price))<0){
            emitService.HelpSendError(emitter,"积分不足");
            return emitter;
        }



        //get data from gpt and send front
        emitService.HelpSendResponse(mapper,executor,question,model,key,emitter,client,detailAsk,sessionsation2);
        //模型次数加1
        addModel(model);
        score = score.subtract(BigInteger.valueOf(price));
        user.setScored_points(score);
        userService.updateUserInfo(user);
        if(question.size()==2){
            String title = getTitle(question.get(1).getContent());
            conversationService.updataTitle(title,sessionId);

        }

        return emitter;
    }

    @Async
    protected void addModel(String model) {
        if(Boolean.FALSE.equals(redisTemplate.hasKey("modelLists" + model))){
            Model model1 = modelService.getModelByName(model);
            redisTemplate.opsForValue().set("modelLists"+model, String.valueOf(model1.getUsed()));
            redisTemplate.expire("modelLists"+model, 40, TimeUnit.MINUTES);
        }
        redisTemplate.opsForValue().increment("modelLists"+model,1);
        rabbitTemplate.convertAndSend("directExchange_use_queue","modularised",model);
    }

    private String getTitle(String content) {
        if (content.length() <=15){
            return content;
        }
        return content.substring(0,15);
    }

    @PostMapping(value = "/message")
    public StreamingResponseBody message(HttpServletResponse response,@RequestParam String token,
                                         @RequestBody DataPromt DataPromt,@RequestParam String sessionId){
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"data.txt\"");
        if (!ver(token)){
            return outputStream -> {
                outputStream.write("请求失败,请重新登录".getBytes());
                outputStream.flush();
            };
        }
        Sessionsation sessionsation2 = new Sessionsation();
        Detail_Ask detailAsk = new Detail_Ask();
        String username= gwt.getUsernameFromToken(token);
        String model = DataPromt.getModel();
        Sessionsation sessionsation = new Sessionsation();
        List<Message> question = DataPromt.getMessage();
        if(question.size()==2){
            String title = getTitle(question.get(1).getContent());
            conversationService.updataTitle(title,sessionId);
        }
        User user = userService.findByUsername(username);
        if(user == null){
            return outputStream -> {
                outputStream.write("请求失败,请重新登录".getBytes());
                outputStream.flush();
            };
        }
        Model needmodel = modelService.getModelByName(model);
        if(needmodel == null){
            return outputStream -> {
                outputStream.write("未找到模型".getBytes());
                outputStream.flush();
            };
        }
        Timestamp created_at = new Timestamp(System.currentTimeMillis());
        Integer id = conversationService.getConversationId(sessionId);
        String context = question.get(question.size()-1).getContent();
        detailAsk.setUser_id(user.getId());
        detailAsk.setQuestion(context);
        detailAsk.setSession_id(id);
        detailAsk.setModel(model);
        detailAsk.setCreated_at(created_at);

        sessionsation.setRole("user");
        sessionsation.setCreated_at(created_at);
        sessionsation.setSession_id(id);
        sessionsation.setContent(context);
        sessionsationService.save(sessionsation);


        sessionsation2.setRole("assistant");
        sessionsation2.setSession_id(id);
        BigInteger score = user.getScored_points();

        Integer price = needmodel.getPrice();

        if(score.compareTo(BigInteger.valueOf(price))<0){
            return outputStream -> {
                detailAsk.setAsk("积分不足");
                sessionsation2.setContent("积分不足");
                sessionsation2.setCreated_at(new Timestamp(System.currentTimeMillis()));
                detailAskService.save(detailAsk);
                sessionsationService.save(sessionsation2);
                outputStream.write("积分不足".getBytes());
                outputStream.flush();
            };
        }
        //模型次数加1
        addModel(model);
        score = score.subtract(BigInteger.valueOf(price));
        user.setScored_points(score);
        userService.updateUserInfo(user);

        BlockingQueue<String> queue = new LinkedBlockingQueue<>();
        ExecutorService executor = Executors.newSingleThreadExecutor();
//        new Thread(()->{
//            helpsendMessage(model,question,key,queue);
//        }).start();
        executor.submit(()->{
            try {
                helpsendMessage(model,question,key,queue);
            } catch (IOException | InterruptedException e) {
                System.err.println("Error in helpsendMessage: " + e.getMessage());
            }
        });
        final StringBuilder allContent = new StringBuilder();
        return outputStream -> {
            try{
                String data;
                while (!Objects.equals(data = queue.poll(20, TimeUnit.SECONDS), " [DONE]")) {
                    //System.out.println("data: "+data);
                    assert data != null;
                    if (data.equals("服务器出错")){
                        allContent.setLength(0);
                        allContent.append("服务器出错");
                        outputStream.write(data.getBytes());
                        outputStream.flush();
                        break;
                    }
                    //System.out.println("发送数据: "+data);
                    allContent.append(data);
                    outputStream.write(data.getBytes());
                    outputStream.flush();
                }
            } catch (InterruptedException e) {
                allContent.setLength(0);
                allContent.append("网路异常");
                logger.error("network error: {}", e.getMessage(), e);
                outputStream.write("网路异常".getBytes());
                outputStream.flush();
//                throw new RuntimeException(e);
            } catch (IOException e){
                //System.out.println(logger.atError());
                allContent.setLength(0);
                allContent.append("数据未生成完整断开连接请重新提问");
                logger.error("Connection lost while writing to output stream: {}", e.getMessage(), e);
            } finally{
                detailAsk.setAsk(allContent.toString());
                sessionsation2.setContent(allContent.toString());
                sessionsation2.setCreated_at(new Timestamp(System.currentTimeMillis()));
                detailAskService.save(detailAsk);
                sessionsationService.save(sessionsation2);
                executor.shutdown();
            }
        };




//        return outputStream -> {
//            for (int i = 0; i < 100; i++) {
//                String data = "Chunk " + i + "\n";
//                outputStream.write(data.getBytes());
//                outputStream.flush(); // Flush after each chunk to send it immediately
//                try {
//                    Thread.sleep(100); // Simulate delay between chunks
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        };
    }
    private void helpsendMessage(String model, List<Message> question, String key,BlockingQueue<String> queue) throws IOException, InterruptedException {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        ObjectMapper mapper = new ObjectMapper();
        String url = "https://api.gptgod.online/v1/chat/completions";
        Map<String, Object> jsonMap = Map.of(
                "model", model,
                "messages", question,
                "stream", true
        );
        String json = mapper.writeValueAsString(jsonMap);
        //System.out.println(json);
        Request httpRequest = new Request.Builder()
                .url(url)
                .post(okhttp3.RequestBody.create(json, okhttp3.MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + key)
                .build();
        Response response = client.newCall(httpRequest).execute();
        //System.out.println(response.isSuccessful());
        if(response.isSuccessful()){
            ResponseBody responseBody = response.body();
            //ResponseBody responseBody = (ResponseBody) response.body();
            //System.out.println(responseBody);
            if (responseBody == null) {
                System.out.println("Response body is null");
            }
            assert responseBody != null;
            BufferedSource source = responseBody.source();
            Buffer buffer = new Buffer();
            //System.out.println(source.exhausted());

            while (!source.exhausted()){
                String line = source.readUtf8Line();
                //System.out.println(line);
                if (line != null && line.startsWith("data: ")){
                    String dataStr = line.substring(5);
                    if (dataStr.equals(" [DONE]")){
                        queue.put(dataStr);
                        break;
                    }

                    JsonNode jsonNode = mapper.readTree(dataStr);
                    String content = extractContent(jsonNode, mapper);
                    //System.out.println("content数据: "+content);
                    if (content != null && !content.isEmpty()){
                        //System.out.println("开始放入数据: "+content);
                        queue.put(content);
                    }
                }
            }
        }
        else{
            queue.put("服务器出错");
        }
    }

    private String extractContent(JsonNode jsonNode, ObjectMapper mapper) {
        // 处理 JSON 内容提取
        return getString(jsonNode);
    }

    @Nullable
    public static String getString(JsonNode jsonNode) {
        try {
            if (jsonNode.isArray()) {
                for (JsonNode node : jsonNode) {
                    JsonNode choices = node.get("choices");
                    if (choices.isArray() && !choices.isEmpty()) {
                        String finish_reason = String.valueOf(choices.get(0).get("finish_reason"));
                        //System.out.println(finish_reason);
                        try {
                            JsonNode delta = choices.get(0).get("delta");

                            return delta.get("content").asText();
                        }catch (Exception e){
                            return null;
                        }
                    }
                }
            } else if (jsonNode.isObject()) {
                JsonNode choices = jsonNode.get("choices");
                //System.out.println("choices: "+choices);
                if (choices.isArray() && !choices.isEmpty()) {
                    String finish_reason = String.valueOf(choices.get(0).get("finish_reason"));
                    //System.out.println("finish_reason: "+finish_reason);
                    try {
                        JsonNode delta = choices.get(0).get("delta");

                        return delta.get("content").asText();
                    }catch (Exception e){
                        return null;
                    }

                }
            }
        } catch (Exception e) {
            logger.error("get data error: {}", e.getMessage(), e);
        }
        return null;
    }


}

