package cn.gugufish.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageService {
    String uploadAvatar(int id,MultipartFile file) throws IOException;
}
