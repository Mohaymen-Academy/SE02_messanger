package com.mohaymen.model.entity;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.json_item.Views;
import com.mohaymen.model.supplies.UpdateType;
import jakarta.persistence.*;
import lombok.*;

@RequiredArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Table(name = "Update")
public class Update {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @JsonView(Views.ChatDisplay.class)
    private Long id;

    @Column(name = "chat_id")
    @NonNull
    private String chatId;

    @Column(name = "type")
    @NonNull
    @JsonView(Views.ChatDisplay.class)
    private UpdateType updateType;

    @Column(name = "message_id")
    @NonNull
    @JsonView(Views.ChatDisplay.class)
    private Long messageId;

    @Transient
    @Setter
    @JsonView(Views.ChatDisplay.class)
    private Message message;

}
