package com.mohaymen.model.entity;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.json_item.Views;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.type.descriptor.jdbc.VarbinaryJdbcType;

@Entity
@Table(name = "MediaFile")
@Getter
@Setter
@NoArgsConstructor
public class MediaFile {

    @JsonView({Views.GetCompressedPicture.class,
            Views.GetOriginalPicture.class, Views.GetMessage.class,
            Views.GetMedia.class})
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "media_id")
    private Long mediaId;

    @NotEmpty
    @JsonView({Views.GetOriginalPicture.class, Views.GetMedia.class,Views.GetMessage.class})
    @Column(name = "media_name", nullable = false)
    private String mediaName;

    @Lob
    @JsonView({Views.GetOriginalPicture.class})
    @JdbcType(VarbinaryJdbcType.class)
    @Column(name = "content", nullable = false)
    private byte[] content;

    @NotEmpty
    @JsonView({Views.GetMedia.class, Views.GetMessage.class})
    @Column(name = "content_type", nullable = false)
    private String contentType;

    @JsonView({Views.GetMedia.class, Views.GetMessage.class})
    @Column(name = "content_size", nullable = false)
    private double contentSize;

    @Lob
    @JdbcType(VarbinaryJdbcType.class)
    @JsonView({Views.GetCompressedPicture.class})
    @Column(name = "compressed_content")
    private byte[] compressedContent;

    @Lob
    @JdbcType(VarbinaryJdbcType.class)
    @JsonView({Views.GetMessage.class,
            Views.ChatDisplay.class,
            Views.MemberInfo.class,
            Views.GetMedia.class,
            Views.ProfileLoginInfo.class,
            Views.ProfileInfo.class})
    @Column(name = "preloading_content")
    private byte[] preLoadingContent;

}
