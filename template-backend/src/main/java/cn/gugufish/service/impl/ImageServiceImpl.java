package cn.gugufish.service.impl;

import cn.gugufish.entity.dto.Account;
import cn.gugufish.entity.dto.StoreImage;
import cn.gugufish.mapper.AccountMapper;
import cn.gugufish.mapper.ImageStoreMapper;
import cn.gugufish.service.ImageService;
import cn.gugufish.utils.Const;
import cn.gugufish.utils.FlowUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.minio.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
@Slf4j
@Service
public class ImageServiceImpl extends ServiceImpl<ImageStoreMapper, StoreImage> implements ImageService {
    @Resource
    MinioClient minioClient;
    @Resource
    AccountMapper accountMapper;
    @Resource
    FlowUtils flowUtils;

    private final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
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
            String avatar = accountMapper.selectById(id).getAvatar();
            minioClient.putObject(args);
            if(accountMapper.update(null, Wrappers.<Account>update()
                    .eq("id",id).set("avatar",imageName))>0){
                this.deleteOldAvatar(avatar);
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
    public String uploadImage(int id , MultipartFile file) throws IOException {
        String key = Const.FORUM_IMAGE_COUNTER + id;
        if(!flowUtils.limitPeriodCounterCheck(key, 20, 3600))
            return null;
        String imageName = UUID.randomUUID().toString().replace("-", "");
        Date date = new Date();
        imageName = "/cache/" + format.format(date) + "/" + imageName;
        PutObjectArgs args = PutObjectArgs.builder()
                .bucket("forum")
                .stream(file.getInputStream(), file.getSize(), -1)
                .object(imageName)
                .build();
        try {
            minioClient.putObject(args);

            if(this.save(new StoreImage(id, imageName, date))) {
                return imageName;
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("图片上传出现问题: "+ e.getMessage(), e);
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
    private void deleteOldAvatar(String avatar) throws Exception {
        if(avatar == null || avatar.isEmpty()) return;
        RemoveObjectArgs remove = RemoveObjectArgs.builder()
                .bucket("forum")
                .object(avatar)
                .build();
        log.info("对象存储清理老的头像");
        minioClient.removeObject(remove);
    }
}
