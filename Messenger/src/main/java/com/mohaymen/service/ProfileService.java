package com.mohaymen.service;

import com.mohaymen.model.entity.*;
import com.mohaymen.model.json_item.ProfileInfo;
import com.mohaymen.model.supplies.ChatType;
import com.mohaymen.model.supplies.ContactID;
import com.mohaymen.model.supplies.ProfilePareId;
import com.mohaymen.repository.*;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Getter
@Service
public class ProfileService {

    private final ProfilePictureRepository profilePictureRepository;

    private final ProfileRepository profileRepository;

    private final MediaFileRepository mediaFileRepository;

    private final ChatParticipantRepository cpRepository;

    private final ServerService serverService;

    private final SearchService searchService;

    private final ContactService contactService;

    private final BlockRepository blockRepository;

    private final AccountService accountService;

    public ProfileService(ProfilePictureRepository profilePictureRepository,
                          ProfileRepository profileRepository,
                          MediaFileRepository mediaFileRepository,
                          ChatParticipantRepository cpRepository,
                          ServerService serverService,
                          SearchService searchService, ContactService contactService, BlockRepository blockRepository, AccountService accountService) {
        this.profilePictureRepository = profilePictureRepository;
        this.profileRepository = profileRepository;
        this.mediaFileRepository = mediaFileRepository;
        this.cpRepository = cpRepository;
        this.serverService = serverService;
        this.searchService = searchService;
        this.contactService = contactService;
        this.blockRepository = blockRepository;
        this.accountService = accountService;
    }

    private void editProfileName(Profile profile, String name, boolean isUser) {
        profile.setProfileName(name);
        profileRepository.save(profile);
        if (!isUser)
            serverService.sendMessage(profile.getType().name().toLowerCase()
                    + " نام خود را به " + name+" تغییر داد ", profile);
    }

    private void editBiography(Profile profile, String newBio, boolean isUser) {
        profile.setBiography(newBio);
        profileRepository.save(profile);
        if (!isUser)
            serverService.sendMessage(profile.getType().name().toLowerCase()
                    + " بیوگرافی خود را به " + newBio+" تغییر داد", profile);
    }

    private void editUsername(Profile profile, String newHandle) {
        profile.setHandle(newHandle);
        profileRepository.save(profile);
    }

    public boolean isNewHandleValid(Long profileId, String newHandle) {
        return !profileRepository.existsByHandleAndProfileIDNot(newHandle, profileId);
    }

    public boolean editInfo(Long userId, Long profileId, String newName, String newBio, String newUsername) {
        Profile profile = hasPermission(userId, profileId);
        if (profile == null)
            return false;
        boolean isUser = profile.getType() == ChatType.USER;
        if (newName != null)
            editProfileName(profile, newName, isUser);
        if (newBio != null)
            editBiography(profile, newBio, isUser);
        if (newUsername != null)
            editUsername(profile, newUsername);

        searchService.updateUser(profile);

        return true;
    }

    public Profile getProfile(Long profileId) {
        Optional<Profile> optionalProfile = profileRepository.findById(profileId);
        if (optionalProfile.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return optionalProfile.get();
    }

    public Profile hasPermission(Long userId, Long profileId) {
        Profile profile = profileRepository.findById(profileId).get();
        Profile user = profileRepository.findById(userId).get();
        if (profile.getType() == ChatType.USER) {
            if (!userId.equals(profileId))
                return null;
        } else {
            ProfilePareId profilePareId = new ProfilePareId(user, profile);
            Optional<ChatParticipant> profilePareIdOptional = cpRepository.findById(profilePareId);
            if (profilePareIdOptional.isEmpty())
                return null;
            if (!profilePareIdOptional.get().isAdmin())
                return null;
        }
        return profile;
    }

    public ProfileInfo getInfo(Long userId, Long profileId) {
        Profile profile = profileRepository.findById(profileId).get();
        Profile user = profileRepository.findById(userId).get();
        ContactID contactID = new ContactID(user, profile);
        boolean isContact = contactService.contactExists(contactID) != null;
        Optional<Block> blockOptional = blockRepository.findById(new ProfilePareId(profile, user));
        List<MediaFile> preLoadingProfiles = new ArrayList<>();
        profile.setStatus(accountService.getLastSeen(profileId));
        if (blockOptional.isEmpty()) {
            List<ProfilePicture> profilePictures = profilePictureRepository.findByProfile_ProfileID(profileId);
            for (ProfilePicture profilePicture : profilePictures) {
                preLoadingProfiles.add(profilePicture.getMediaFile());
            }
        }
        return ProfileInfo.builder()
                .isContact(isContact)
                .profile(profile)
                .profilePictures(preLoadingProfiles)
                .build();
    }

    public int getAccessPermission(Profile user, Profile chat) {
        //0 when you are not the admin and can not send a message in a channel
        //or blocked by each other
        ChatParticipant chatParticipant;
        try {
            chatParticipant = getParticipant(user, chat);
        } catch (Exception e) {
            return chat.getType() == ChatType.USER ? 1 : 0;
        }

        if (chat.getType() == ChatType.CHANNEL && !chatParticipant.isAdmin()) return 0;
        if (chat.getType() == ChatType.USER) {
            Optional<Block> blockPt1 = blockRepository.findById(new ProfilePareId(user, chat));
            Optional<Block> blockPt2 = blockRepository.findById(new ProfilePareId(chat, user));
            if (blockPt1.isPresent() || blockPt2.isPresent())
                return 0;
        }
        //if you are the admin of a chanel or group you've got permission to do whatever
        //can remove or edit his/her messages
        return chat.getType() != ChatType.USER && chatParticipant.isAdmin() ? 2 : 1;
    }
    public ChatParticipant getParticipant(Profile user, Profile dest) throws Exception {
        Optional<ChatParticipant> participant = cpRepository.findById(new ProfilePareId(user, dest));
        if (participant.isEmpty())
            throw new Exception("user is not a member of this chat");
        return participant.get();
    }

}
