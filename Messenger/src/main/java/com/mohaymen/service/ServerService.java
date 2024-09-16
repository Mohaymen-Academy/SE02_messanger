package com.mohaymen.service;

import com.mohaymen.model.entity.*;
import com.mohaymen.model.supplies.Status;
import com.mohaymen.security.*;
import com.mohaymen.repository.*;
import lombok.Getter;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Service
public class ServerService {

    private final ProfileRepository profileRepository;

    private final MessageRepository messageRepository;

    private final ChatParticipantRepository cpRepository;

    private final AccountRepository accountRepository;

    private final SearchService searchService;

    private static Profile server;

    private Profile baseChannel;

    private Profile baseProfile;

    public ServerService(ProfileRepository profileRepository,
                         MessageRepository messageRepository,
                         ChatParticipantRepository cpRepository,
                         AccountRepository accountRepository,
                         SearchService searchService) {
        this.messageRepository = messageRepository;
        this.cpRepository = cpRepository;
        this.accountRepository = accountRepository;
        this.searchService = searchService;
        this.profileRepository = profileRepository;
        createRasaServer();
        createRasaAdmin();
        createRasaChannel();
    }

    private void createRasaServer() {
        Optional<Profile> server_tmp = profileRepository.findById(1L);
        if (server_tmp.isEmpty())
            server = createServerAccounts(profileRepository, false, 3, 1L, "SERVER", "#SERVER");
        else
            server = server_tmp.get();
    }

    private void createRasaChannel() {
        Optional<Profile> baseChannel_tmp = profileRepository.findById(3L);
        if (baseChannel_tmp.isPresent()) {
            baseChannel = baseChannel_tmp.get();
            return;
        }
        baseChannel = createServerAccounts(profileRepository, false, 2, 3L, "پیام رسان رسا✔", "#MESSENGER-BASE-CHANNEL");
        sendMessage("این کانال ساخته شد", baseChannel);
        cpRepository.save(new ChatParticipant(baseProfile, baseChannel, baseChannel.getHandle(), true));
        baseChannel.setDefaultProfileColor("#59b35f");
        baseChannel.setMemberCount(1);
        profileRepository.save(baseChannel);
        searchService.addChannel(baseChannel);
    }

    private void createRasaAdmin() {
        Optional<Profile> baseAccount_tmp = profileRepository.findById(2L);
        if (baseAccount_tmp.isPresent()) {
            baseProfile = baseAccount_tmp.get();
            return;
        }
        baseProfile = createServerAccounts(profileRepository, false, 0, 2L, "اعلان های رسا✔", "#MESSENGER-BASE-ACCOUNT");
        baseProfile.setDefaultProfileColor("#295c4c");
        baseProfile.setBiography("کانال رسمی پیام رسان رسا");
        profileRepository.save(baseProfile);
        byte[] accountSalt = SaltGenerator.getSaltArray();
        byte[] password = "kimia ali sara sana".getBytes();
        Account account = new Account(
                baseProfile.getProfileID(),
                baseProfile,
                PasswordHandler.configPassword(password, accountSalt),
                "rasaa.messenger.team@gmail.com",
                Status.DEFAULT, LocalDateTime.now(),
                false, accountSalt);
        accountRepository.save(account);
        searchService.addUser(account);
    }

    public static Profile getServer() {
        return server;
    }

    private Profile createServerAccounts(ProfileRepository profileRepository, boolean isDeleted, int type,
                                         Long profileId, String profileName, String handle) {
        return profileRepository.insertProfile(isDeleted, type, profileId, profileName, handle);
    }

    public void sendMessage(String messageText, Profile receiver) {
        Message message = new Message();
        message.setSender(server);
        message.setReceiver(receiver);
        message.setText(messageText);
        message.setTime(Instant.now());
        message.setViewCount(0);
        messageRepository.save(message);
    }

}
