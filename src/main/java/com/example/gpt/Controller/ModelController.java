package com.example.gpt.Controller;

import com.example.gpt.DAO.Model;
import com.example.gpt.Service.ModelService;
import com.example.gpt.utils.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/model")
@CrossOrigin("*")
@Tag(name = "模型管理")
public class ModelController {
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    ModelService modelService;
    @Operation(summary = "获取模型列表",description = "获取模型列表")
    @PostMapping("/getModelList")
    public Result getModelList() throws JsonProcessingException {
        //System.out.println(redisTemplate.hasKey("ModelList"));
        if (Boolean.FALSE.equals(redisTemplate.hasKey("modelLists"))){
            System.out.println("查询数据库");
            List<Model>model = modelService.getModelList();
            String jsonModelList = objectMapper.writeValueAsString(model);
            redisTemplate.opsForValue().set("modelLists", jsonModelList);
            redisTemplate.expire("modelLists", 40, TimeUnit.MINUTES);
            for(Model m:model){
                if (Boolean.FALSE.equals(redisTemplate.hasKey("modelLists" + m.getName()))){
                    redisTemplate.opsForValue().set("modelLists"+m.getName(), String.valueOf(m.getUsed()));
                    redisTemplate.expire("modelLists"+m.getName(), 40, TimeUnit.MINUTES);
                }
            }
            return Result.success(model);
        }
        System.out.println("使用缓存");
        String cachedModelList = redisTemplate.opsForValue().get("modelLists");
        //System.out.println(cachedModelList);
        List<Model> modelList = objectMapper.readValue(cachedModelList, new TypeReference<List<Model>>() {});
        for (Model m : modelList){
            m.setUsed(Integer.parseInt(Objects.requireNonNull(redisTemplate.opsForValue().get("modelLists" + m.getName()))));
        }
        //System.out.println(modelList);
        return Result.success(modelList);
    }
}
