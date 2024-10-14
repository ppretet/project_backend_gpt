package com.example.gpt.DAO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "模型对象")
public class Model implements Serializable {
    //private Integer id;
    @Schema(description = "模型名称", example = "gpt-3.5-turbo")
    private String name;
    @Schema(description = "模型价格")
    private Integer price;
    @Schema(description = "模型使用次数")
    private Integer used;
}
