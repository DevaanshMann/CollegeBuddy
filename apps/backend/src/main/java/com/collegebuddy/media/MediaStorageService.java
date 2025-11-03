package com.collegebuddy.media;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/*
    Handles avatar upload/storage and returns a URL
 */

@Service
public class MediaStorageService {

    public String storeAvatar(MultipartFile file, Long userID) {
//        TODO: validate size/type, upload to storage, return URL
        return "https://cdn.example.com/avatar/" + userID;
    }
}
