package com.mohaymen.web;

import com.mohaymen.security.JwtHandler;
import com.mohaymen.repository.LogRepository;
import com.mohaymen.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PinChatController extends PinController {

    public PinChatController(PinMessageService pm_service, LogRepository logRepository,
                             PinChatService pinChatService) {
        super(pm_service, logRepository, pinChatService);
    }

    @PutMapping("/pin-chat/{chatId}")
    public ResponseEntity<String> addToPins(@RequestHeader(name = "Authorization") String token,
                                            @PathVariable Long chatId) {
        long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("User id is not acceptable!");
        }
        try {
            pinChatService.pinChat(userId, chatId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Failed pin chat : " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/unpin-chat/{chatId}")
    public ResponseEntity<String> unpinChat(@RequestHeader(name = "Authorization") String token,
                                            @PathVariable Long chatId) {
        long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("User id is not acceptable!");
        }
        try {
            pinChatService.unpinChat(userId, chatId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Failed unpin chat : " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

}
