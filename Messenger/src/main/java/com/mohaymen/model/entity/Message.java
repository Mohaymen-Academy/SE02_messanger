package com.mohaymen.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.json_item.ReplyMessageInfo;
import com.mohaymen.model.json_item.Views;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Builder
@AllArgsConstructor
@Table(name = "Message")
public class Message {

    @JsonView({Views.GetMessage.class, Views.ChatDisplay.class, Views.GetMedia.class})
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageID;

    @JsonView({Views.GetMessage.class, Views.ChatDisplay.class})
    @Column(name = "text",columnDefinition = "TEXT")
    private String text;

    @JsonView({Views.GetMessage.class, Views.ChatDisplay.class})
    @Column(name = "text_style",columnDefinition = "TEXT")
    private String textStyle;

    @JsonView({Views.GetMessage.class, Views.ChatDisplay.class})
    @Column(name = "time", nullable = false)
    private Instant time;

    @JsonView({Views.GetMessage.class, Views.GetMedia.class})
    @OneToOne
    @JoinColumn(name = "fk_media_id", referencedColumnName = "media_id")
    private MediaFile media;

    @JsonView({Views.GetMessage.class, Views.ChatDisplay.class})
    @Column(name = "view_count", nullable = false, columnDefinition = "int default 0")
    private Integer viewCount;

    @JsonView(Views.GetMessage.class)
    @ManyToOne
    @JoinColumn(name = "fk_sender", referencedColumnName = "profile_id", nullable = false)
    private Profile sender;

    @ManyToOne
    @JoinColumn(name = "fk_receiver", referencedColumnName = "profile_id", nullable = false)
    private Profile receiver;

    @Column(name = "reply_message_id")
    private Long replyMessageId;

    @JsonView(Views.GetMessage.class)
    @Transient
    private ReplyMessageInfo replyMessageInfo;

    @Column(name = "forward_message_id")
    private Long forwardMessageId;

    @JsonView(Views.GetMessage.class)
    @Transient
    private String forwardMessageSender;

    @JsonView(Views.GetMessage.class)
    @JsonProperty(value="isEdited")
    @Column(name = "is_edited")
    private boolean isEdited;

    @JsonView({Views.GetMessage.class, Views.ChatDisplay.class})
    @Column(name = "message_preview")
    private String messagePreview;

    public Message addView() {
        this.viewCount++;
        return this;
    }

}
