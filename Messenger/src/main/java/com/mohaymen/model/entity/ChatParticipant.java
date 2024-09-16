package com.mohaymen.model.entity;

import com.mohaymen.model.supplies.ProfilePareId;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@RequiredArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@IdClass(ProfilePareId.class)
@Table(name = "Chat_participant")
public class ChatParticipant {

    @Id
    @ManyToOne
    @JoinColumn(name = "fk_profile_1", referencedColumnName = "profile_id")
    @NonNull
    private Profile user;

    @Id
    @ManyToOne
    @JoinColumn(name = "fk_profile_2", referencedColumnName = "profile_id")
    @NonNull
    private Profile destination;

    @Setter
    @Column(name = "chat_id")
    @NonNull
    private String chatId;

    @Setter
    @Column(name = "is_admin", nullable = false)
    @NonNull
    private boolean isAdmin;

    @Setter
    @Column(name = "last-update")
    private Long lastUpdate;

    @Setter
    @Column(name = "is_pinned", columnDefinition = "boolean default false")
    private boolean isPinned;

    @Setter
    @ManyToOne
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "pinned_msg",referencedColumnName = "message_id")
    private Message pinnedMessage;

}
