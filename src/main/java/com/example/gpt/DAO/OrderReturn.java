package com.example.gpt.DAO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderReturn {
    private String order_id;
    private String doc_id;
    private String doc_detail;
    private double price;
    private Timestamp create_time;
    private String pay_link;

}
