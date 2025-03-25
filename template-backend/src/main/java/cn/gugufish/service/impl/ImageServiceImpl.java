package cn.gugufish.service.impl;

import cn.gugufish.entity.dto.Account;
import cn.gugufish.mapper.AccountMapper;
import cn.gugufish.service.ImageService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
@Slf4j
@Service
public class ImageServiceImpl implements ImageService {
    @Resource
    MinioClient minioClient;
    @Resource
    AccountMapper accountMapper;
    @Override
    public String uploadAvatar(int id, MultipartFile file) throws IOException {
        String imageName = UUID.randomUUID().toString().replace("-", "");
        imageName = "/avatar/" + imageName;
        PutObjectArgs args = PutObjectArgs.builder()
                .bucket("forum")
                .stream(file.getInputStream(),file.getSize(),-1)
                .object(imageName)
                .build();
        try{
            minioClient.putObject(args);
            if(accountMapper.update(null, Wrappers.<Account>update()
                    .eq("id",id).set("avatar",imageName))>0){
                return imageName;
            }
            else{
                return null;
            }
        }catch (Exception e){
            log.error("图片上传发生问题"+e.getMessage(),e);
            return null;
        }
    }

    @Override
    public void fetchImageFromMinio(OutputStream stream, String image) throws Exception {
        GetObjectArgs args = GetObjectArgs.builder()
                .bucket("forum")
                .object(image)
                .build();
        GetObjectResponse response = minioClient.getObject(args);
        IOUtils.copy(response, stream);
    }
}
