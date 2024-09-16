package com.mohaymen.model.json_item;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.entity.MediaFile;
import com.mohaymen.model.entity.Profile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@JsonView(Views.ProfileInfo.class)
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileInfo {

    public Profile profile;

    public boolean isContact;

    public List<MediaFile> profilePictures;

    public static class ProfileInfoBuilder {
        public ProfileInfo.ProfileInfoBuilder profilePictures(List<MediaFile> mediaFiles) {
            mediaFiles.forEach(x -> x.setPreLoadingContent(x.getContent()));
            this.profilePictures = mediaFiles;
            return this;
        }
        public ProfileInfo.ProfileInfoBuilder profile(Profile profile) {
            if(profile.getLastProfilePicture() != null)
                profile.getLastProfilePicture().setPreLoadingContent(
                    profile.getLastProfilePicture().getContent() );
            this.profile = profile;
            return this;
        }
    }

}
