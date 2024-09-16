package com.mohaymen.web;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.json_item.Views;
import com.mohaymen.security.JwtHandler;
import com.mohaymen.repository.LogRepository;
import com.mohaymen.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController
public class PinMessageController extends PinController {

    private final LogService logger;

    public PinMessageController(PinMessageService pm_service,
                                LogRepository logRepository,
                                PinChatService pinChatService) {
        super(pm_service, logRepository, pinChatService);
        this.logger = new LogService(logRepository, PinMessageController.class.getName());
    }

    @PutMapping("/pinMessage")
    public ResponseEntity<String> pinMessage(@RequestBody Map<String, Object> messageReq,
                                             @RequestHeader(name = "Authorization") String token) {
        Long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
        }
        Long messageId = ((Number) messageReq.get("messageId")).longValue();
        try {
            pm_Service.setPinMessage(userId, messageId, true);
            return ResponseEntity.ok().body("Message is pinned");
        } catch (Exception e) {
            logger.error("Failed pin message: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/unpinMessage")
    public ResponseEntity<String> unpinMessage(@RequestBody Map<String, Object> messageReq,
                                               @RequestHeader(name = "Authorization") String token) {
        Long userID;
        try {
            userID = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
        }
        Long messageId = ((Number) messageReq.get("messageId")).longValue();
        try {
            pm_Service.setPinMessage(userID, messageId, false);
            return ResponseEntity.ok().body("Message is unpinned");
        } catch (Exception e) {
            logger.error("Failed unpin message: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @JsonView(Views.GetMessage.class)
    @GetMapping("/getPinnedMessages/{chatId}")
    public ResponseEntity<?> getPinnedMessages(@PathVariable Long chatId,
                                               @RequestHeader(name = "Authorization") String token) {
        Long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
        }
        try {
            return ResponseEntity.ok().body(pm_Service.getPinMessage(userId, chatId));
        } catch (Exception e) {
            logger.error("Failed get pinned messages: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("fail");
        }
    }

}
