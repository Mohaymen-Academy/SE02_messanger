package com.mohaymen.model.json_item;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.entity.Message;
import com.mohaymen.model.entity.Profile;
import lombok.Getter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@JsonView(Views.GetMessage.class)
@Getter
public class MessageDisplay {

    private final boolean isUpFinished;

    private final boolean isDownFinished;

    private final List<Message> messages;

    private final Long messageId;

    private final DateTimeFormatter formatter;

    public MessageDisplay(List<Message> upMessages,
                          List<Message> downMessages,
                          Message message,
                          boolean isDownFinished,
                          boolean isUpFinished,
                          Long lastSeenMessage,
                          Profile serverProfile) {
        formatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd").withZone(ZoneId.of("Asia/Tehran"));
        this.isDownFinished = isDownFinished;
        this.isUpFinished = isUpFinished;
        this.messages = new ArrayList<>();
        for (int i = upMessages.size() - 1; i >= 0; i--) {
            addMessage(upMessages.get(i), lastSeenMessage, serverProfile, this.isUpFinished);
        }
        addMessage(message, lastSeenMessage, serverProfile, this.isUpFinished);
        downMessages.forEach(m -> addMessage(m, lastSeenMessage, serverProfile, this.isUpFinished));
        this.messageId = message != null ? message.getMessageID()
                : upMessages.isEmpty() ? 0
                : upMessages.get(0).getMessageID();
    }

    private void addMessage(Message message, Long lastSeenMessage, Profile serverProfile, Boolean isUpFinished) {
        if (message == null)
            return;
        if (messages.size() == 0) {
            if(isUpFinished)
                messages.add(getServerMessage(0L, formatter.format(message.getTime()),
                        message.getTime(), message.getReceiver(), serverProfile));
//            if(lastSeenMessage < message.getMessageID()){
//                messages.add(getServerMessage(-1l,"پیام های خوانده نشده",
//                        message.getTime(), message.getReceiver(), serverProfile));
//            }
            messages.add(message);
            return;
        }
        Message lastMessage = messages.get(messages.size() - 1);
        if (!formatter.format(lastMessage.getTime()).equals(formatter.format(message.getTime()))) {
            messages.add(getServerMessage(0L, formatter.format(message.getTime()),
                    message.getTime(), message.getReceiver(), serverProfile));
        }
//        if(lastSeenMessage < message.getMessageID() && lastSeenMessage >= lastMessage.getMessageID()){
//            messages.add(getServerMessage(-1L,"پیام های خوانده نشده",
//                    message.getTime(), message.getReceiver(), serverProfile));
//        }
        messages.add(message);
    }

    private Message getServerMessage(Long id, String text, Instant time, Profile receiver, Profile serverProfile) {
        Message serverMessage = new Message();
        serverMessage.setMessageID(id);
        serverMessage.setText(text);
        serverMessage.setTime(time);
        serverMessage.setSender(serverProfile);
        serverMessage.setReceiver(receiver);
        serverMessage.setTextStyle("");
        serverMessage.setViewCount(0);
        return serverMessage;
    }

}
