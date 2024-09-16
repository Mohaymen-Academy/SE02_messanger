package com.mohaymen.web;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.entity.*;
import com.mohaymen.model.json_item.*;
import com.mohaymen.model.supplies.ChatType;
import com.mohaymen.repository.LogRepository;
import com.mohaymen.security.JwtHandler;
import com.mohaymen.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
public class ChatController {

    private final ChatService chatService;

    private final MediaService mediaService;

    private final LogService logger;

    public ChatController(ChatService chatService,
                          MediaService mediaService,
                          LogRepository logRepository) {
        this.chatService = chatService;
        this.mediaService = mediaService;
        this.logger = new LogService(logRepository, ChatController.class.getName());
    }

    @JsonView(Views.ChatDisplay.class)
    @GetMapping("/")
    public ResponseEntity<ChatListInfo> getChats(@RequestHeader(name = "Authorization") String token,
                                                 @RequestParam(name = "limit", defaultValue = "20") int limit) {
        Long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        }
        try {
            ChatListInfo chatListInfo = chatService.getChats(userId, limit);
            return ResponseEntity.ok().body(chatListInfo);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Failed get chats: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }


    @PostMapping("/create-chat")
    public ResponseEntity<String> createChat(@RequestHeader(name = "Authorization") String token,
                                             @RequestBody Map<String, Object> request) {
        ChatType type;
        Long userId;
        MediaFile mediaFile;
        List<Long> membersId = new ArrayList<>();
        String name = (String) request.get("name");
        String bio = (String) request.get("bio");
        try {
            type = ChatType.values()[((Number) request.get("type")).intValue()];
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cast error!");
        }
        try {
            membersId = (List<Long>) request.get("members");
        } catch (Exception ignored) {
        }
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("User id is not acceptable!");
        }
        Long profileId;
        try {
            profileId = chatService.createChat(userId, name, type, bio, membersId);
            try {
                mediaFile = mediaService.uploadFile(request);
                if (mediaFile != null)
                    mediaService.addProfilePicture(userId, profileId, mediaFile);
            } catch (Exception ignored) {
            }
            logger.info("Successful create chat : id = " + profileId);
            return ResponseEntity.ok().body("successful");
        } catch (Exception e) {
            logger.error("Fail create chat: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("fail");
        }
    }

    @PostMapping("/add-member")
    public ResponseEntity<String> addMember(@RequestHeader(name = "Authorization") String token,
                                            @RequestBody Map<String, Object> request) {
        long userId, chatId, memberId;
        try {
            chatId = ((Number) request.get("chatId")).longValue();
            memberId = ((Number) request.get("memberId")).longValue();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cast error!");
        }
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("User id is not acceptable!");
        }
        try {
            chatService.addMember(userId, chatId, memberId);
            return ResponseEntity.ok().body("successful");
        } catch (Exception e) {
            logger.error("Failed add member: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You are not allowed to add this user!");
        }
    }

    @PostMapping("/join-channel/{channelId}")
    public ResponseEntity<String> joinChannel(@PathVariable Long channelId,
                                              @RequestHeader(name = "Authorization") String token) {
        Long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("User id is not acceptable!");
        }
        try {
            chatService.joinChannel(userId, channelId);
            return ResponseEntity.ok().body("successful");
        } catch (Exception e) {
            logger.error("Failed join channel: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed");
        }
    }

    @PostMapping("/add-admin")
    public ResponseEntity<String> addAdmin(@RequestHeader(name = "Authorization") String token,
                                           @RequestBody Map<String, Object> request) {
        long userId, chatId, memberId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("User id is not acceptable!");
        }
        try {
            chatId = ((Number) request.get("chatId")).longValue();
            memberId = ((Number) request.get("memberId")).longValue();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cast error!");
        }
        try {
            chatService.addAdmin(userId, chatId, memberId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Failed add admin: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/delete-chat")
    public ResponseEntity<String> deleteChat(@RequestHeader(name = "Authorization") String token,
                                             @RequestBody Map<String, Object> request) {
        Long chatId = Long.parseLong((String) request.get("chatId"));
        Long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("User id is not acceptable!");
        }
        try {
            chatService.deleteChat(userId, chatId);
            logger.info("Successful delete chat: id = " + chatId);
            return ResponseEntity.ok().body("successful");
        } catch (Exception e) {
            logger.error("Failed delete chat: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/leave")
    public ResponseEntity<String> leaveChat(@RequestHeader(name = "Authorization") String token,
                                            @RequestBody Map<String, Object> request) {
        long userId, chatId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("User id is not acceptable!");
        }
        try {
            chatId = ((Number) request.get("chatId")).longValue();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cast error!");
        }
        try {
            chatService.leaveChat(userId, chatId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Failed leave chat: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @JsonView(Views.MemberInfo.class)
    @GetMapping("/{chatId}/members")
    public ResponseEntity<List<Profile>> getMembers(@RequestHeader(name = "Authorization") String token,
                                                    @PathVariable Long chatId) {
        long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        }
        try {
            return ResponseEntity.ok().body(chatService.getMembers(userId, chatId));
        } catch (Exception e) {
            logger.error("Failed get members: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{chatId}/check")
    public ResponseEntity<Boolean> isMemberOfChannel(@RequestHeader(name = "Authorization") String token,
                                                     @PathVariable Long chatId) {
        long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        }
        try {
            return ResponseEntity.ok().body(chatService.isMemberOfChannel(userId, chatId));
        } catch (Exception e) {
            logger.error("Failed check if a user is member of channel: " + e.getMessage());
            return ResponseEntity.badRequest().body(false);
        }
    }

}
