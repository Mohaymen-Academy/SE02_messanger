package com.mohaymen.service;

import com.mohaymen.model.entity.*;
import com.mohaymen.repository.*;
import com.mohaymen.model.supplies.*;
import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.*;
import java.time.LocalDate;
import java.util.*;
import com.mohaymen.model.json_item.LoginInfo;
import com.mohaymen.security.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class AccessService {

    private final AccountRepository accountRepository;

    private final ChatParticipantService chatParticipantService;

    private final AccountService accountService;

    private final ProfileRepository profileRepository;

    private final SearchService searchService;

    private final MessageService messageService;

    private final JavaMailSender mailSender;

    public AccessService(AccountRepository accountRepository,
                         ChatParticipantService chatParticipantService,
                         AccountService accountService,
                         ProfileRepository profileRepository,
                         SearchService searchService,
                         MessageService messageService,
                         JavaMailSender mailSender) {
        this.accountRepository = accountRepository;
        this.chatParticipantService = chatParticipantService;
        this.accountService = accountService;
        this.profileRepository = profileRepository;
        this.searchService = searchService;
        this.messageService = messageService;
        this.mailSender = mailSender;

        JwtHandler.setVERSION_KEY(UUID.randomUUID().toString());
    }

    public Account emailExists(String email) {
        Optional<Account> account = accountRepository.findByEmail(email);
        if(account.isEmpty())
            return null;
        return account.get();
    }

    /**
     * Authenticates a user by their email and password.
     *
     * @param email    The password of the user, as a byte array.
     * @param password the password of the user as a byte array
     * @return The login information of the user, including a success message, JWT token, profile information, and last seen timestamp.
     * @throws Exception If the email is not found or the password is incorrect.
     */
    public LoginInfo login(String email, byte[] password) throws Exception {
        Optional<Account> account = accountRepository.findByEmail(email);
        if (account.isEmpty())
            throw new Exception("Account not found");

        byte[] checkPassword = PasswordHandler.getHashed(
                PasswordHandler.combineArray(password, account.get().getSalt()));

        if (!Arrays.equals(checkPassword, account.get().getPassword()))
            throw new Exception("Wrong password");
        accountService.UpdateLastSeen(account.get().getId());

        return LoginInfo.builder()
                .message("success")
                .jwt(JwtHandler.generateAccessToken(account.get().getId()))
                .profile(account.get().getProfile())
                .lastSeen(accountService.getLastSeen(account.get().getId()))
                .build();
    }

    private Profile createProfile(String handle, String name){
        Profile profile = new Profile();
        profile.setHandle(handle);
        profile.setProfileName(name);
        profile.setType(ChatType.USER);
        profile.setDefaultProfileColor(generateColor(handle));
        return profile;
    }

    private Account createAccount(Profile profile, String email, byte[] password){
        byte[] salt = SaltGenerator.getSaltArray();
        Account account = new Account();
        account.setId(profile.getProfileID());
        account.setProfile(profile);
        account.setEmail(email);
        account.setLastSeen(LocalDateTime.now());
        account.setPassword(PasswordHandler.configPassword(password, salt));
        account.setStatus(Status.DEFAULT);
        account.setSalt(salt);
        return account;
    }

    public void signup(String name, String email) throws Exception {
        if (emailExists(email) != null)
            throw new Exception("information is not valid");
        String verificationCode = convertEmailToFourDigitNumber(email);
        sendEmail(name, email, verificationCode,"<h3>[[name]] عزیز ،<br> </h3>"
                + " </h3><h3>کد تایید شما:<br>"
                + "<h1>[[code]]</h1>"
                + "<h3>باتشکر،<br>"
                + "پیامرسان رسا"
                + "</h3>", "لطفا ثبت نام خود را تایید کنید");
    }

    public void forgetPassword(String email) throws Exception {
        Account account = emailExists(email);
        if(account == null)
            throw new Exception("you have not signed up or invalid email");
        String generatedPass = generateRandomPassword();
        byte[] salt = SaltGenerator.getSaltArray();
        account.setPassword(PasswordHandler.configPassword(generatedPass.getBytes(), salt));
        account.setSalt(salt);
        accountRepository.save(account);
        sendEmail(null, email, generatedPass, " </h3><h3>رمز عبور جدید شما:<br>"
                + "<h1>[[code]]</h1>"
                + "<h3>باتشکر،<br>"
                + "پیامرسان رسا"
                + "</h3>", "بازگردانی رمز عبور");
    }

    private String generateRandomPassword(){
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            int randomIndex = random.nextInt(characters.length());
            sb.append(characters.charAt(randomIndex));
        }
        return sb.toString();
    }

    public LoginInfo verify(String email, String name, byte[] password, String inputCode) throws Exception {
        String actualCode = convertEmailToFourDigitNumber(email);
        if(inputCode.equals(actualCode)) {
            return completeSignup(email, name, password);
        }
        throw new Exception("Information is not valid.");
    }

    private LoginInfo completeSignup(String email, String name, byte[] password) throws Exception {
        Profile profile = createProfile(email, name);
        profileRepository.save(profile);
        Account account = createAccount(profile, email, password);
        accountRepository.save(account);
        //add user to search index
        searchService.addUser(account);
        //add user to the messenger channel
        welcomeUserInitialize(profile);
        return LoginInfo.builder()
                .message("success")
                .jwt(JwtHandler.generateAccessToken(account.getId()))
                .profile(account.getProfile())
                .lastSeen(accountService.getLastSeen(account.getId()))
                .build();
    }

    private String convertEmailToFourDigitNumber(String email) throws NoSuchAlgorithmException {
        email = email + LocalDate.now();
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(email.getBytes());
        BigInteger no = new BigInteger(1, messageDigest);
        String hashedEmail = no.toString(16);
        return hashedEmail.substring(0, 4);
    }

    private void sendEmail(String name, String email, String code, String emailText, String emailSubject)
            throws MessagingException, UnsupportedEncodingException {
        String fromAddress = "rasaa.messenger.team@gmail.com";
        String senderName = "پیامرسان رسا";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message,true, "UTF-8");

        helper.setFrom(fromAddress, senderName);
        helper.setTo(email);
        helper.setSubject(emailSubject);

        if(emailText.contains("[[name]]"))
            emailText = emailText.replace("[[name]]", name);
        if(emailText.contains("[[code]]"))
            emailText = emailText.replace("[[code]]", code);

        helper.setText(emailText, true);

        mailSender.send(message);
    }

    private void welcomeUserInitialize(Profile profile) throws Exception {
        Profile baseChannel = profileRepository.findById(3L).get();
        Profile baseAccount = profileRepository.findById(2L).get();
        messageService.sendMessage(baseAccount.getProfileID(), profile.getProfileID(), "به پیام رسان رسا خوش آمدید",
                "", null, null, null);
        chatParticipantService.createChatParticipant(profile, baseChannel, false);
    }

    public static String generateColor(String inputString) {
        int seed = inputString.hashCode();
        Random random = new Random(seed);
        int hue = random.nextInt(360);
        Color color = Color.getHSBColor(hue / 360f, 0.5f, 0.9f);
        return String.format("#%06x", color.getRGB() & 0x00FFFFFF);
    }

}