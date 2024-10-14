package com.example.gpt.DAO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private Integer id;
    private String order_id;
    private String goods_id;
    private String status;
    private Timestamp created_at;
    private Integer user_id;
    private String pay_link;
    private Timestamp finish_at;
}
