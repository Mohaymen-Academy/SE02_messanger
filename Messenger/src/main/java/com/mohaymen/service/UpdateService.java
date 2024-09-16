package com.mohaymen.service;

import com.mohaymen.model.entity.*;
import com.mohaymen.model.supplies.*;
import com.mohaymen.repository.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UpdateService {

    private final UpdateRepository updateRepository;

    private final MessageRepository messageRepository;

    private final ChatParticipantRepository cpRepository;

    public UpdateService(UpdateRepository updateRepository,
                         MessageRepository messageRepository,
                         ChatParticipantRepository cpRepository) {
        this.updateRepository = updateRepository;
        this.messageRepository = messageRepository;
        this.cpRepository = cpRepository;
    }

    public void setNewUpdate(Message message, UpdateType type) {
        Optional<ChatParticipant> cpOptional =
                cpRepository.findById(new ProfilePareId(message.getSender(), message.getReceiver()));
        if (cpOptional.isPresent()) {
            ChatParticipant cp = cpOptional.get();
            String chatId = cp.getChatId();
            Update update = new Update(chatId, type, message.getMessageID());
            updateRepository.save(update);
            List<Message> messages = messageRepository.findByReplyMessageId(message.getMessageID());
            for (Message m : messages) {
                if (type == UpdateType.DELETE) {
                    m.setReplyMessageId(null);
                    messageRepository.save(m);
                }
                update = new Update(chatId, UpdateType.EDIT, m.getMessageID());
                updateRepository.save(update);
            }
        }
    }

}
