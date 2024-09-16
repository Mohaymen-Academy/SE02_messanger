package com.mohaymen.service;

import com.mohaymen.model.entity.ChatParticipant;
import com.mohaymen.model.entity.Profile;
import com.mohaymen.model.supplies.ChatType;
import com.mohaymen.model.supplies.ProfilePareId;
import com.mohaymen.repository.ChatParticipantRepository;
import com.mohaymen.repository.ProfileRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class ChatParticipantService {

    private final ChatParticipantRepository cpRepository;

    private final ProfileRepository profileRepository;

    public ChatParticipantService(ChatParticipantRepository cpRepository,
                                  ProfileRepository profileRepository) {
        this.cpRepository = cpRepository;
        this.profileRepository = profileRepository;
    }

    public boolean doesNotChatParticipantExist(Profile user, Profile destination) {
        return cpRepository.findById(new ProfilePareId(user, destination)).isEmpty();
    }

    public void createChatParticipant(Profile user, Profile profile, boolean isAdmin) {
        String id = profile.getType() != ChatType.USER ? profile.getHandle() : createRandomId();
        if (doesNotChatParticipantExist(user, profile)) {
            cpRepository.save(new ChatParticipant(user, profile, id, isAdmin));
            if (profile.getType() != ChatType.USER) {
                profile.setMemberCount(profile.getMemberCount() + 1);
                profileRepository.save(profile);
            }
        }
        if (profile.getType() == ChatType.USER &&
                doesNotChatParticipantExist(profile, user))
            cpRepository.save(new ChatParticipant(profile, user, id, false));
    }

    private String createRandomId() {
        UUID uuid = UUID.randomUUID();
        List<ChatParticipant> chatParticipants = cpRepository.findByChatId(uuid.toString());
        if (!chatParticipants.isEmpty()) return createRandomId();
        return uuid.toString();
    }

}
