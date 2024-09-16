package com.mohaymen.service;

import com.mohaymen.model.entity.*;
import com.mohaymen.model.supplies.*;
import com.mohaymen.repository.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PinMessageService extends PinService {

    private final UpdateService updateService;

    protected PinMessageService(ChatParticipantRepository cpRepository,
                                BlockRepository blockRepository,
                                ProfileRepository profileRepository,
                                MessageRepository messageRepository,
                                UpdateService updateService) {
        super(cpRepository, blockRepository, profileRepository, messageRepository);
        this.updateService = updateService;
    }

    public void setPinMessage(Long userID, Long messageId, boolean pin) throws Exception {
        Message message = checkIsPossible(userID, messageId);
        Profile user = getProfile(userID);
        Profile chat = message.getReceiver().getProfileID().equals(userID)
                ? message.getSender() : message.getReceiver();
        if (chat.getType() == ChatType.USER) {
            ChatParticipant chatParticipant1 = null, chatParticipant2 = null;
            try {
                chatParticipant1 = getParticipant(user, chat);
            } catch (Exception ignore) {
            }
            if (getBlockParticipant(user, chat) == null && getBlockParticipant(chat, user) == null) {
                setPin(chatParticipant1, pin, message);
                try {
                    chatParticipant2 = getParticipant(chat, user);
                } catch (Exception ignore) {
                }
                setPin(chatParticipant2, pin, message);
                updateService.setNewUpdate(message, pin ? UpdateType.PIN : UpdateType.UNPIN);
            } else throw new Exception("could not pin the message,due to block");
        } else {
            List<ChatParticipant> destinations = cpRepository.findByDestination(chat);
            for (ChatParticipant p : destinations) setPin(p, pin, message);
            updateService.setNewUpdate(message, pin ? UpdateType.PIN : UpdateType.UNPIN);
        }
    }

    private void setPin(ChatParticipant p, boolean pin, Message message) {
        if (p != null) {
            p.setPinnedMessage(pin ? message : null);
            cpRepository.save(p);
        }
    }

    public Message getPinMessage(Long userId, Long chatId) throws Exception {
        Profile user = getProfile(userId);
        Profile chat = getProfile(chatId);
        ChatParticipant chatParticipant = getParticipant(user, chat);
        return chatParticipant.getPinnedMessage();
    }

}
