package com.mohaymen.service;

import com.mohaymen.model.entity.*;
import com.mohaymen.model.supplies.ChatType;
import com.mohaymen.model.supplies.ProfilePareId;
import com.mohaymen.repository.*;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public abstract class PinService {

    final ChatParticipantRepository cpRepository;

    final BlockRepository blockRepository;

    final ProfileRepository profileRepository;

    final MessageRepository messageRepository;

    protected PinService(ChatParticipantRepository cpRepository, BlockRepository blockRepository, ProfileRepository profileRepository, MessageRepository messageRepository) {
        this.cpRepository = cpRepository;
        this.blockRepository = blockRepository;
        this.profileRepository = profileRepository;
        this.messageRepository = messageRepository;
    }

    public Block getBlockParticipant(Profile user, Profile chat) {
        Optional<Block> blockParticipant = blockRepository.findById(new ProfilePareId(user, chat));
        return blockParticipant.orElse(null);
    }

    public Profile getProfile(Long profileId) throws Exception {
        Optional<Profile> optionalProfile = profileRepository.findById(profileId);
        if (optionalProfile.isEmpty()) throw new Exception("profile not found!");
        return optionalProfile.get();
    }

    public ChatParticipant getParticipant(Profile user, Profile dest) throws Exception {
        ProfilePareId profilePareId = new ProfilePareId(user, dest);
        Optional<ChatParticipant> participant = cpRepository.findById(profilePareId);
        if (participant.isEmpty())
            throw new Exception("user is not a member of this chat");
        return participant.get();
    }

    private Message getMessage(Long messageId) throws Exception {
        Optional<Message> msg = messageRepository.findById(messageId);
        if (msg.isEmpty())
            throw new Exception("Message doesn't exist");
        return msg.get();
    }

    public Message checkIsPossible(Long userID, Long messageId) throws Exception {
        Message message = getMessage(messageId);
        Profile chat = message.getReceiver();
        Profile user = getProfile(userID);
        if (chat.getType() != ChatType.USER) {
            ChatParticipant chatParticipant = getParticipant(user, chat);
            if (!chatParticipant.isAdmin())
                throw new Exception("this user is not the admin of the chat");
        }
        return message;
    }

}
