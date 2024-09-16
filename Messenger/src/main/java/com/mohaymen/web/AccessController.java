package com.mohaymen.web;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.json_item.LoginInfo;
import com.mohaymen.model.json_item.Views;
import com.mohaymen.repository.LogRepository;
import com.mohaymen.security.JwtHandler;
import com.mohaymen.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/access")
public class AccessController {

    private final AccountService accountService;

    private final AccessService accessService;

    private final LogService logger;

    public AccessController(AccountService accountService, AccessService accessService, LogRepository logRepository) {
        this.accountService = accountService;
        this.accessService = accessService;
        this.logger = new LogService(logRepository, AccessController.class.getName());
    }

    @JsonView(Views.ProfileLoginInfo.class)
    @PostMapping("/login")
    public ResponseEntity<LoginInfo> login(@RequestBody Map<String, Object> requestBody) {
        String email;
        byte[] password;
        try {
            email = (String) requestBody.get("email");
            password = ((String) requestBody.get("password")).getBytes();
        } catch (Exception e) {
            logger.info("Failed login: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new LoginInfo("fail"));
        }
        try {
            LoginInfo loginInfo = accessService.login(email, password);
            logger.info("Successful login: " + email);
            return ResponseEntity.ok().body(loginInfo);
        } catch (Exception e) {
            logger.info("Failed login: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body(new LoginInfo("ایمل یا رمز عبور اشتباه است"));
        }
    }

    @GetMapping("/signup")
    public ResponseEntity<String> isValidSignUpInfo(@RequestParam(name = "email") String email) {
        if (accessService.emailExists(email) == null) {
            logger.info("Successful Signup Validation : " + email);
            return ResponseEntity.ok().body("success");
        }
        logger.info("Failed Signup Validation : email exits");
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("fail");
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody Map<String, Object> requestBody) {
        String name;
        String email;
        try {
            name = (String) requestBody.get("name");
            email = (String) requestBody.get("email");
        } catch (Exception e) {
            logger.info("Failed signup: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("fail");
        }
        try {
            accessService.signup(name, email);
            logger.info("Successful signup: name = " + name + ", email = " + email);
            return ResponseEntity.ok().body("ok");
        } catch (Exception e) {
            logger.info("Failed signup: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("fail");
        }
    }

    @PutMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, Object> requestBody){
        String email;
        try {
            email = (String) requestBody.get("email");
        } catch (Exception e) {
            logger.info("Failed" + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("fail");
        }
        try {
            accessService.forgetPassword(email);
            return ResponseEntity.ok().body("ok");
        } catch (Exception e) {
            logger.info("Failed signup: " + Arrays.toString(e.getStackTrace()));
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body("fail");
        }
    }

    @JsonView(Views.ProfileLoginInfo.class)
    @PostMapping("/verify-signup")
    public ResponseEntity<LoginInfo> verifySignup(@RequestBody Map<String, Object> requestBody){
        String name;
        String email;
        byte[] password;
        String inputCode;
        try {
            name = (String) requestBody.get("name");
            email = (String) requestBody.get("email");
            password = ((String) requestBody.get("password")).getBytes();
            inputCode = (String) requestBody.get("code");
        } catch (Exception e) {
            logger.info("Failed signup: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new LoginInfo("fail"));
        }
        try {
            LoginInfo loginInfo = accessService.verify(email, name, password, inputCode);
            return ResponseEntity.ok().body(loginInfo);
        } catch (Exception e) {
            logger.info("Failed signup: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body(new LoginInfo("fail"));
        }
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<String> deleteAccount(@RequestHeader(name = "Authorization") String token,
                                                @RequestBody Map<String, Object> accountInfo) {
        byte[] password = ((String) accountInfo.get("password")).getBytes();
        Long id;
        try {
            id = JwtHandler.getIdFromAccessToken(token);
            accountService.deleteAccount(id, password);
            logger.info("Account of user with id " + id + " successfully deleted.");
        } catch (Exception e) {
            logger.info("Failed delete account " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok().body("successful");
    }

}
