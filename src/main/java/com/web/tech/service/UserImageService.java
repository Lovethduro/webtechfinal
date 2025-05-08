package com.web.tech.service;

import com.web.tech.model.UserImage;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Optional;

public interface UserImageService {
    UserImage saveImage(String userId, MultipartFile file) throws IOException;
    Optional<UserImage> getImage(String userId);
    void deleteImage(String userId);

    Optional<UserImage> getImageByUserId(String userId);
}