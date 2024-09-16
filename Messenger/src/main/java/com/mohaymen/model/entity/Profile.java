package com.mohaymen.model.entity;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.supplies.ChatType;
import com.mohaymen.model.json_item.Views;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "Profile")
public class Profile {

    @JsonView({Views.GetMessage.class, Views.ChatDisplay.class,
            Views.ProfileLoginInfo.class, Views.ProfileInfo.class,
            Views.MemberInfo.class})
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "profile_id_generator")
    @SequenceGenerator(name="profile_id_generator", sequenceName = "profile_seq", initialValue = 1000)
    @Column(name = "profile_id")
    private Long profileID;

    @NonNull
    @JsonView({Views.ProfileLoginInfo.class, Views.ProfileInfo.class})
    @NotEmpty
    @Column(name = "handle")
    private String handle;


    @NonNull
    @JsonView({Views.GetMessage.class, Views.ChatDisplay.class,
            Views.ProfileLoginInfo.class, Views.ProfileInfo.class,
            Views.ProfileInfo.class, Views.MemberInfo.class})
    @NotEmpty
    @Column(name = "profile_name", length = 50, nullable = false)
    private String profileName;

    @JsonView({Views.ProfileLoginInfo.class, Views.ProfileInfo.class})
    @Column(name = "bio",columnDefinition = "TEXT")
    private String biography;

    @Column(name = "user_count")
    private Integer memberCount;

    @NonNull
    @JsonView({Views.ChatDisplay.class, Views.ProfileInfo.class})
    @Column(name = "type", nullable = false)
    private ChatType type;

    @NonNull
    @JsonView({Views.GetMessage.class, Views.ChatDisplay.class,
            Views.ProfileLoginInfo.class, Views.ProfileInfo.class,
            Views.ProfileInfo.class, Views.MemberInfo.class})
    @Column(name = "default_profile_color")
    private String defaultProfileColor;

    @JsonView({Views.GetMessage.class, Views.ChatDisplay.class,
            Views.ProfileLoginInfo.class, Views.ProfileInfo.class, Views.MemberInfo.class})
    @OneToOne
    @JoinColumn(name = "fk_mediaFile_id", referencedColumnName = "media_id")
    private MediaFile lastProfilePicture;

    @JsonView(Views.MemberInfo.class)
    @Column(name = "is_deleted")
    private boolean isDeleted;

    @Transient
    @JsonView({Views.ChatDisplay.class,Views.ProfileLoginInfo.class, Views.MemberInfo.class})
    private String status;

    @Transient
    @JsonView({Views.ChatDisplay.class})
    private int accessPermission;

}
