package com.mohaymen.web;

import com.mohaymen.repository.LogRepository;
import com.mohaymen.service.LogService;
import com.mohaymen.service.PinChatService;
import com.mohaymen.service.PinMessageService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public abstract class PinController {

    final PinMessageService pm_Service;

    final PinChatService pinChatService;

    final LogRepository logRepository;

    final LogService logger;

    public PinController(PinMessageService pm_service, LogRepository logRepository, PinChatService pinChatService) {
        pm_Service = pm_service;
        this.logRepository = logRepository;
        this.logger = new LogService(logRepository, AccessController.class.getName());
        this.pinChatService = pinChatService;
    }

}
