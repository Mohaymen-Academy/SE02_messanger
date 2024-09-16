package com.mohaymen.model.entity;

import com.mohaymen.model.supplies.Status;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "Account")
public class Account {

    @Id
    private Long id;

    @OneToOne
    @JoinColumn(name = "profile_id")
    private Profile profile;

    @Column(name = "password", nullable = false)
    private byte[] password;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    @Column(name = "last_seen_setting")
    @ColumnDefault("false")
    private boolean lastSeenSetting;

    @Column(name = "salt", nullable = false)
    private byte[] salt;

}
