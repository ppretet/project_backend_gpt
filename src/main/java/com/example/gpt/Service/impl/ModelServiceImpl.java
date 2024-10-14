package com.example.gpt.Service.impl;

import com.example.gpt.DAO.Model;
import com.example.gpt.Service.ModelService;
import com.example.gpt.mapper.ModelMapper;
import com.example.gpt.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModelServiceImpl implements ModelService {
    @Autowired
    ModelMapper modelMapper;
    @Override
    public List<Model> getModelList() {
        try {
            return (modelMapper.getAllModel());
        }
        catch (Exception e){
            throw new RuntimeException(String.valueOf(e));
        }
    }

    @Override
    public Model getModelByName(String name) {
        return modelMapper.getModelByName(name);
    }

    @Override
    public int updateUsed(Model model) {
        modelMapper.updateUsed(model);
        return 1;
    }
}
