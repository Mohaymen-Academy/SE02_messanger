package com.mohaymen.model.json_item;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@JsonView(Views.GetMessage.class)
@Setter
@Getter
@AllArgsConstructor
public class ReplyMessageInfo {

    private Long messageId;

    private String sender;

    private String text;

    private byte[] compressedContent;

    private String messagePreview;
}
