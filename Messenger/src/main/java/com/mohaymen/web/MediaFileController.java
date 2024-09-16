package com.mohaymen.web;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.entity.MediaFile;
import com.mohaymen.model.json_item.Views;
import com.mohaymen.repository.LogRepository;
import com.mohaymen.security.JwtHandler;
import com.mohaymen.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController
public class MediaFileController {

    private final LogService logger;

    private final MediaService mediaService;

    public MediaFileController(MediaService mediaService, LogRepository logRepository) {
        this.mediaService = mediaService;
        this.logger = new LogService(logRepository, MediaFileController.class.getName());
    }

    @PostMapping("/profile/picture/{id}")
    public ResponseEntity<String> addProfilePicture(@PathVariable Long id, @RequestBody Map<String, Object> data,
                                                    @RequestHeader(name = "Authorization") String token) {
        MediaFile mediaFile;
        Long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("fail");
        }
        try {
            mediaFile = mediaService.uploadFile(data);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("fail");
        }
        try {
            if (!mediaService.addProfilePicture(userId, id, mediaFile))
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("fail");
        } catch (Exception e) {
            logger.error("failed add profile picture :" + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("fail with exception");
        }
        return ResponseEntity.ok().body("successful");
    }

    @ResponseBody
    @DeleteMapping("/profile/picture/{id}/{mediaFileId}")
    public ResponseEntity<String> deleteProfilePhoto(@PathVariable Long id,
                                                     @PathVariable Long mediaFileId,
                                                     @RequestHeader(name = "Authorization") String token) {
        Long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("invalid jwt");
        }
        try {
            if (!mediaService.deleteProfilePicture(userId, id, mediaFileId))
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("have not permission");
        } catch (Exception e) {
            logger.error("failed delete profile picture :" + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("fail with exception");
        }
        return ResponseEntity.status(HttpStatus.OK).body("successfully deleted");
    }

    @JsonView(Views.GetOriginalPicture.class)
    @GetMapping("/original/{mediaId}")
    public ResponseEntity<MediaFile> getOriginalMedia(@PathVariable Long mediaId,
                                                      @RequestHeader(name = "Authorization") String token) {
        try {
            JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
        }
        return ResponseEntity.ok().body(mediaService.getOriginalMedia(mediaId));
    }

    @JsonView(Views.GetCompressedPicture.class)
    @GetMapping("/compressed/{mediaId}")
    public ResponseEntity<MediaFile> getCompressedPicture(@PathVariable Long mediaId,
                                                          @RequestHeader(name = "Authorization") String token) {
        try {
            JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        }
        return ResponseEntity.ok().body(mediaService.getCompressedPicture(mediaId));
    }

}
