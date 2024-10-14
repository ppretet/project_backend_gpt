package com.example.gpt.Service.impl;

import com.example.gpt.Service.IndexService;
import com.example.gpt.mapper.IndexMapper;
import com.example.gpt.utils.Result;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IndexServiceImpl implements IndexService {
    @Autowired
    IndexMapper indexMapper;
    @Override
    public Result getOldSessionList(Integer id) {
        return Result.success(indexMapper.getReturnRecory(id));
    }
}
