package com.mohaymen.model.json_item;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.entity.Message;
import com.mohaymen.model.entity.Profile;
import com.mohaymen.model.entity.Update;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@JsonView(Views.ChatDisplay.class)
@Getter
@Builder
@AllArgsConstructor
public class ChatDisplay {

    private Profile profile;

    private Message lastMessage;

    private int unreadMessageCount;

    private List<Update> updates;

    private boolean isPinned;

    private boolean hasBlockedYou;

}
