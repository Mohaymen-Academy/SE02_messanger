package com.mohaymen.web;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.json_item.*;
import com.mohaymen.security.JwtHandler;
import com.mohaymen.service.ProfileService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/profile")
public class ProfilesController {

    private final ProfileService profileService;

    public ProfilesController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/username-validation")
    public ResponseEntity<String> isNewUsernameValid(@RequestHeader(name = "Authorization") String token,
                                                     @RequestBody Map<String, Object> request) {
        Long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("invalid jwt");
        }
        String newUsername = (String) request.get("username");
        if(!profileService.isNewHandleValid(userId, newUsername))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid username");
        return ResponseEntity.ok().body("valid username");
    }

    @PutMapping("/edit-info/{id}")
    public ResponseEntity<String> editInfo(@PathVariable Long id,
                                           @RequestHeader(name = "Authorization") String token,
                                           @RequestBody Map<String, Object> request) {
        Long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("invalid jwt");
        }
        String newName = (String) request.get("name");
        String newBio = (String) request.get("biography");
        String newUsername = (String) request.get("username");
        if(!profileService.editInfo(userId, id, newName, newBio, newUsername))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid edit");
        return ResponseEntity.ok().body("successful");
    }

    @JsonView(Views.ProfileInfo.class)
    @GetMapping("/info/{id}")
    public ResponseEntity<ProfileInfo> getInfo(@PathVariable Long id,
                               @RequestHeader(name = "Authorization") String token) {
        Long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        }
        return ResponseEntity.ok().body(profileService.getInfo(userId, id));
    }

}
