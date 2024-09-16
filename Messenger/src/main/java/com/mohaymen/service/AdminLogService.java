package com.mohaymen.service;

import com.mohaymen.model.entity.Log;
import com.mohaymen.repository.LogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminLogService {

    private final LogRepository logRepository;

    public AdminLogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public List<Log> getLastLogs(int limit, String logger){
        if(logger.equals("-")){
            return logRepository.findAllByOrderByIdDesc(PageRequest.of(0, limit));
        }
        return logRepository.findByLoggerOrderByIdDesc(logger, PageRequest.of(0, limit));
    }
}
