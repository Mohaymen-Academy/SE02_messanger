package com.mohaymen.web;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.entity.Profile;
import com.mohaymen.model.json_item.Views;
import com.mohaymen.repository.LogRepository;
import com.mohaymen.security.JwtHandler;
import com.mohaymen.service.ContactService;
import com.mohaymen.service.LogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/contacts")
public class ContactController {

    private final ContactService contactService;

    private final LogService logger;

    public ContactController(ContactService contactService,
                             LogRepository logRepository){
        this.contactService = contactService;
        this.logger = new LogService(logRepository, ContactController.class.getName());
    }

    @JsonView(Views.ChatDisplay.class)
    @PostMapping("/{id}")
    public ResponseEntity<Profile> addContact(@PathVariable Long id, @RequestHeader(name = "Authorization") String token,
                                              @RequestBody Map<String, Object> contactInfo){
        String customName = (String) contactInfo.get("customName");
        Long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        }
        try {
            Profile profileDisplay = contactService.addContact(userId, id, customName);
            return ResponseEntity.ok().body(profileDisplay);
        } catch (Exception e){
            logger.error("failed add contact: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @Transactional
    @DeleteMapping("/")
    public ResponseEntity<String> deleteContact(@RequestHeader(name = "Authorization") String token,
                                              @RequestBody Map<String, Object> contactInfo){
        Long contactId = ((Number) contactInfo.get("id")).longValue();
        Long id;
        try {
            id = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("invalid jwt");
        }
        try {
            contactService.deleteContact(id,contactId);
            return ResponseEntity.ok().body("deleted");
        }catch (Exception e){
            logger.error("failed delete contact: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid delete contact");
        }
    }

    @JsonView(Views.ChatDisplay.class)
    @GetMapping("/")
    public ResponseEntity<List<Profile>> getContacts(@RequestHeader(name = "Authorization") String token){
        Long id;
        try {
            id = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        }
        return ResponseEntity.ok().body(contactService.getContactsOfOneUser(id));
    }

    @PutMapping("/edit-custom-name/{id}")
    public ResponseEntity<String>editCustomName(@PathVariable Long id,
                                                @RequestHeader(name = "Authorization") String token,
                                                @RequestBody Map<String, Object> body){
        Long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("failed");
        }
        try {
            contactService.editCustomName(userId, id, (String) body.get("custom-name"));
            return ResponseEntity.ok().body("successful");
        } catch (Exception e){
            logger.error("failed edit contact: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid edit contact");
        }
    }

}
