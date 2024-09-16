package com.mohaymen.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@ToString
@Entity
@Table(name = "Log")
@NoArgsConstructor
@RequiredArgsConstructor
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;

    @NonNull
    @Column(name = "log_time")
    private LocalDateTime logTime;

    @NonNull
    @Column(name = "logger")
    private String logger;

    @NonNull
    @Column(name = "level", length = 15)
    private String level;

    @NonNull
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

}