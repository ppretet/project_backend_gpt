package com.example.gpt.DAO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Goods {
    private String title;
    private String detail;
    private Double price;
    private String goods_id;
    private Timestamp created_at;
}
