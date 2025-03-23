package cn.gugufish.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;

public interface ImageService {
    String uploadAvatar(int id,MultipartFile file) throws IOException;
    void fetchImageFromMinio(OutputStream stream, String image) throws Exception;
}
