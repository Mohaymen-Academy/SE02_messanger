package com.mohaymen.model.json_item;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.entity.Profile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@JsonView(Views.ProfileLoginInfo.class)
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class LoginInfo {

    @NonNull
    public String message;

    public String jwt;

    public Profile profile;

    public String lastSeen;

    public static class LoginInfoBuilder {
        public LoginInfoBuilder profile(Profile profile) {
            if (profile.getLastProfilePicture() != null) {
                profile.getLastProfilePicture().setPreLoadingContent(profile.getLastProfilePicture().getContent());
            }
            this.profile = profile;
            return this;
        }
    }

}
