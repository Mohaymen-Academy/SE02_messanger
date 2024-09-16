package com.mohaymen.service;

import com.mohaymen.model.entity.Log;
import com.mohaymen.repository.LogRepository;

import java.time.LocalDateTime;

public class LogService {

    private enum Level {INFO, DEBUG, ERROR}

    private final LogRepository logRepository;

    private final String logger;

    public LogService(LogRepository logRepository, String logger) {
        this.logRepository = logRepository;
        this.logger = logger;
    }

    public void info(String message) {
        Log log = new Log(LocalDateTime.now(), logger, Level.INFO.name(), message);
        logRepository.save(log);
    }

    public void error(String message) {
        Log log = new Log(LocalDateTime.now(), logger, Level.ERROR.name(), message);
        logRepository.save(log);
    }

    public void debug(String message) {
        Log log = new Log(LocalDateTime.now(), logger, Level.DEBUG.name(), message);
        logRepository.save(log);
    }

}
