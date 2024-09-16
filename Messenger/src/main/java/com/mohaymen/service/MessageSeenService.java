package com.mohaymen.service;

import com.mohaymen.model.entity.*;
import com.mohaymen.model.supplies.*;
import com.mohaymen.repository.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class MessageSeenService {

    private final ProfileRepository profileRepository;

    private final MessageRepository messageRepository;

    private final MessageSeenRepository msRepository;

    private final UpdateService updateService;

    public MessageSeenService(ProfileRepository profileRepository,
                              MessageRepository messageRepository,
                              MessageSeenRepository msRepository,
                              UpdateService updateService) {
        this.profileRepository = profileRepository;
        this.messageRepository = messageRepository;
        this.msRepository = msRepository;
        this.updateService = updateService;
    }

    public synchronized void addMessageView(Long userId, Long messageId) throws Exception {
        Profile user = getProfile(userId);
        Message message = getMessage(messageId);
        Profile destination = !message.getReceiver().getType().equals(ChatType.USER)
                ? message.getReceiver()
                : (message.getSender().getProfileID().equals(userId)
                    ? message.getReceiver() : message.getSender());
        Optional<MessageSeen> messageSeenOptional = msRepository.findById(new ProfilePareId(user, destination));
        MessageSeen messageSeen;
        if (messageSeenOptional.isPresent()) {
            messageSeen = messageSeenOptional.get();
            Long lastMessageSeen = messageSeen.getLastMessageSeenId();
            if (lastMessageSeen < messageId) {
                addAllMessagesViews(lastMessageSeen + 1, messageId, user, destination);
                messageSeen.setLastMessageSeenId(messageId);
            }
        } else {
            messageSeen = new MessageSeen(user, destination, messageId);
            Long firstMessageId = destination.getType() == ChatType.USER
                    ? messageRepository.findPVFirstMessage(user, destination).getMessageID()
                    : messageRepository.findFirstByReceiver(destination).getMessageID();
            addAllMessagesViews(firstMessageId, messageId, user, destination);
        }
        msRepository.save(messageSeen);
    }

    private void addAllMessagesViews(Long minMessageId, Long maxMessageId,
                                     Profile user, Profile destination) {
        List<Message> messages;
        if (destination.getType() == ChatType.USER)
            messages = messageRepository.findMessagesInRange
                    (user, destination, minMessageId, maxMessageId);
        else
            messages = messageRepository.findByReceiverAndMessageIDGreaterThanEqualAndMessageIDLessThanEqual
                    (destination, minMessageId, maxMessageId);
        for (Message message : messages) {
            message.addView();
            messageRepository.save(message);
            if (!message.getSender().getProfileID().equals(user.getProfileID()) &&
                    message.getViewCount() % Math.pow(10, (int) Math.log10(message.getViewCount())) == 0)
                updateService.setNewUpdate(message, UpdateType.SEEN);
        }
    }

    private Message getMessage(Long messageId) throws Exception {
        Optional<Message> messageOptional = messageRepository.findById(messageId);
        if (messageOptional.isEmpty()) throw new Exception("message not found!");
        return messageOptional.get();
    }

    private Profile getProfile(Long profileId) throws Exception {
        Optional<Profile> optionalProfile = profileRepository.findById(profileId);
        if (optionalProfile.isEmpty()) throw new Exception("profile not found!");
        return optionalProfile.get();
    }

}
