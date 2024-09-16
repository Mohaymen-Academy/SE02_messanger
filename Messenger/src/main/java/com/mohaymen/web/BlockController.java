package com.mohaymen.web;

import com.mohaymen.repository.LogRepository;
import com.mohaymen.security.JwtHandler;
import com.mohaymen.service.BlockService;
import com.mohaymen.service.LogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class BlockController {

    private final BlockService blockService;

    private final LogService logger;

    public BlockController(BlockService blockService, LogRepository logRepository) {
        this.blockService = blockService;
        this.logger = new LogService(logRepository, BlockController.class.getName());
    }

    @PostMapping("/block")
    public ResponseEntity<String> blockUser(@RequestHeader(name = "Authorization") String token,
                                            @RequestParam(name = "profile_id") Long block_id) {
        Long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            blockService.blockUser(userId, block_id);
            return ResponseEntity.ok().body("successful");
        } catch (Exception e) {
            logger.error("Fail block user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/unblock")
    public ResponseEntity<String> unblockUser(@RequestHeader(name = "Authorization") String token,
                                              @RequestParam(name = "profile_id")Long block_id) {
        Long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            blockService.unblockUser(userId, block_id);
            return ResponseEntity.ok().body("successful");
        } catch (Exception e) {
            logger.error("Fail unblock user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}


