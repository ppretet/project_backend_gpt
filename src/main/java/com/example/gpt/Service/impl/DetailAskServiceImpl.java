package com.example.gpt.Service.impl;

import com.example.gpt.DAO.Detail_Ask;
import com.example.gpt.Service.DetailAskService;
import com.example.gpt.mapper.DetailAskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DetailAskServiceImpl implements DetailAskService {
    @Autowired
    private DetailAskMapper detailAskMapper;
    @Override
    public void save(Detail_Ask detail_ask) {
        detailAskMapper.save(detail_ask);
    }
}
