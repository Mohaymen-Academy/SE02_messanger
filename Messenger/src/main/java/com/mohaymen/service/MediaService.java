package com.mohaymen.service;

import com.mohaymen.model.entity.*;
import com.mohaymen.model.supplies.ProfilePictureID;
import com.mohaymen.repository.*;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;

@Service
public class MediaService {

    private final ProfileService profileService;

    private final ProfilePictureRepository profilePictureRepository;

    private final AccountService accountService;

    private final MediaFileRepository mediaFileRepository;

    public MediaService(ProfileService profileService,
                        ProfilePictureRepository profilePictureRepository,
                        AccountService accountService,
                        MediaFileRepository mediaFileRepository) {
        this.profileService = profileService;
        this.profilePictureRepository = profilePictureRepository;
        this.accountService = accountService;
        this.mediaFileRepository = mediaFileRepository;
    }

    public boolean addProfilePicture(Long userId, Long profileID, MediaFile picture) throws Exception {
        ProfilePicture profilePicture = new ProfilePicture();
        Profile profile = profileService.hasPermission(userId, profileID);
        if (profile == null)
            return false;
        profilePicture.setProfile(profile);
        profilePicture.setMediaFile(picture);
        profile.setLastProfilePicture(picture);
        profilePictureRepository.save(profilePicture);
        accountService.UpdateLastSeen(userId);
        return true;
    }

    public boolean deleteProfilePicture(Long userId, Long profileId, Long mediaFileId) throws Exception {
        Profile profile = profileService.hasPermission(userId, profileId);
        if (profile == null)
            return false;
        ProfilePictureID profilePictureID = new ProfilePictureID(profile,
                mediaFileRepository.findById(mediaFileId).get());
        profilePictureRepository.delete(profilePictureRepository.findById(profilePictureID).get());
        deleteFile(mediaFileId);
        accountService.UpdateLastSeen(userId);
        return true;
    }

    private void deleteFile(Long mediaId) {
        mediaFileRepository.deleteById(mediaId);
    }

    public MediaFile getOriginalMedia(Long mediaFileId) {
        return mediaFileRepository.findById(mediaFileId).get();
    }

    public MediaFile uploadFile(Map<String, Object> fileData) throws Exception {
        MediaFile mediaFile = new MediaFile();
        String contentStr = (String) fileData.get("content");
        if (contentStr == null)
            return null;
        double fileSize = ((Number) fileData.get("size")).doubleValue();
        String contentType = (String) fileData.get("media-type");
        String fileName = (String) fileData.get("fileName");
        byte[] content = Base64.getDecoder().decode(contentStr);
        mediaFile.setContentSize(fileSize);
        mediaFile.setContentType(contentType);
        mediaFile.setMediaName(fileName);
        mediaFile.setContent(content);
        if(contentType.startsWith("image"))
            addCompressedImage(mediaFile);
        mediaFileRepository.save(mediaFile);
        return mediaFile;
    }

    public MediaFile uploadFile(MediaFile mediaFile){
        if(mediaFile == null)
            return null;
        MediaFile newMedia = new MediaFile();
        newMedia.setContentSize(mediaFile.getContentSize());
        newMedia.setContentType(mediaFile.getContentType());
        newMedia.setMediaName(mediaFile.getMediaName());
        newMedia.setContent(mediaFile.getContent());
        if(mediaFile.getContentType().startsWith("image")) {
            newMedia.setCompressedContent(mediaFile.getCompressedContent());
            newMedia.setPreLoadingContent(mediaFile.getPreLoadingContent());
        }
        mediaFileRepository.save(newMedia);
        return newMedia;
    }

    private void addCompressedImage(MediaFile mediaFile) throws Exception {
        mediaFile.setCompressedContent(compressImage(mediaFile.getContent(), 128, 0.5f, mediaFile.getContentType()));
        mediaFile.setPreLoadingContent(compressImage(mediaFile.getContent(), 8, 1, mediaFile.getContentType()));
        mediaFileRepository.save(mediaFile);
    }

    private byte[] compressImage(byte[] content, int size, float quality, String format) throws Exception {
        ByteArrayInputStream input = new ByteArrayInputStream(content);
        BufferedImage image = ImageIO.read(input);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Thumbnails.of(image)
                .size(size, size)
                .outputFormat(format.substring(format.indexOf("/")+1))
                .outputQuality(quality)
                .toOutputStream(output);

        return output.toByteArray();
    }

    public MediaFile getCompressedPicture(Long mediaId) {
        return mediaFileRepository.findById(mediaId).get();
    }

}
