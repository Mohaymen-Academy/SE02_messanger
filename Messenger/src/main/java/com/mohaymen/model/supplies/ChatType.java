package com.mohaymen.model.supplies;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.json_item.Views;

@JsonView(Views.ChatDisplay.class)
public enum ChatType {

    USER(0),
    GROUP(1),
    CHANNEL(2),
    SERVER(3);

    @JsonValue
    public final int value;

    ChatType(int value) {
        this.value = value;
    }

}
