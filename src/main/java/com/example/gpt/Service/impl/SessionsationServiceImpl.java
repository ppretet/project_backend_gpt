package com.example.gpt.Service.impl;

import com.example.gpt.DAO.Sessionsation;
import com.example.gpt.DAO.User;
import com.example.gpt.Service.SessionsationService;
import com.example.gpt.Service.UserService;
import com.example.gpt.mapper.SessionSationMapper;
import com.example.gpt.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SessionsationServiceImpl implements SessionsationService {
    @Autowired
    private SessionSationMapper sessionSationMapper;
    @Override
    public void save(Sessionsation sessionsation) {
        sessionSationMapper.save(sessionsation);
    }
}
