package com.mohaymen.web;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.entity.Log;
import com.mohaymen.model.json_item.Views;
import com.mohaymen.security.JwtHandler;
import com.mohaymen.service.AdminLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class LogController {

    private final AdminLogService adminLogService;

    public LogController(AdminLogService adminLogService) {
        this.adminLogService = adminLogService;
    }

    @GetMapping("/log")
    public ResponseEntity<List<Log>> getLastLogs(@RequestHeader(name = "Authorization") String token,
                                                 @RequestParam(name = "limit", defaultValue = "20") int limit,
                                                 @RequestParam(name = "logger", defaultValue = "-") String logger){
        Long userID;
        try {
            userID = JwtHandler.getIdFromAccessToken(token);
            if(userID != 2)
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        }

        try {
            return ResponseEntity.status(HttpStatus.OK).body(adminLogService.getLastLogs(limit, logger));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

}
