package com.example.gpt.Service;

import com.example.gpt.DAO.Model;
import com.example.gpt.utils.Result;

import java.util.List;

public interface ModelService {
    List<Model> getModelList();
    Model getModelByName(String name);
    int updateUsed(Model model);
}
