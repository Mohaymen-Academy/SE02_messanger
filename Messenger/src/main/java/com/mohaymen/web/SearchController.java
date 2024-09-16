package com.mohaymen.web;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.supplies.ChatType;
import com.mohaymen.model.entity.*;
import com.mohaymen.model.json_item.*;
import com.mohaymen.repository.*;
import com.mohaymen.security.JwtHandler;
import com.mohaymen.service.SearchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    private final ProfileRepository profileRepository;

    public SearchController(SearchService searchService, ProfileRepository profileRepository) {
        this.searchService = searchService;
        this.profileRepository = profileRepository;
    }

    @JsonView(Views.ChatDisplay.class)
    @GetMapping("/{chatID}")
    public ResponseEntity<List<Message>> searchInChat(@PathVariable Long chatID,
                                                      @RequestHeader(name = "Authorization") String token,
                                                      @RequestParam(name = "search_entry") String searchEntry) {
        Long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        }
        Optional<Profile> profile = profileRepository.findById(chatID);
        if (profile.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        Profile p = profile.get();
        if (p.getType() == ChatType.USER) {
            return ResponseEntity.ok()
                    .body(searchService.searchInPv(userId, chatID, searchEntry));
        } else {
            return ResponseEntity.ok()
                    .body(searchService.searchInChat(chatID, searchEntry));
        }
    }

    @JsonView(Views.ChatDisplay.class)
    @GetMapping("/")
    public ResponseEntity<List<SearchResultItemGroup>> searchGlobal(@RequestHeader(name = "Authorization") String token,
                                                                    @RequestParam(name = "search_entry") String searchEntry) {
        Long userId;
        try {
            userId = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        }
        return ResponseEntity.ok()
                .body(searchService.GlobalSearch(userId, searchEntry));
    }

}
