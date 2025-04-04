package cn.gugufish.controller;

import cn.gugufish.entity.RestBean;
import cn.gugufish.service.ImageService;
import cn.gugufish.utils.Const;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
@Slf4j
@RestController
@RequestMapping("/api/image")
public class ImageController {
    @Resource
    ImageService imageService;
    @PostMapping("cache")
    public RestBean<String> uploadImage(@RequestAttribute(Const.ATTR_USER_ID) int id,
                                        @RequestParam("file") MultipartFile file,
                                        HttpServletResponse response) throws Exception{
        if(file.getSize() > 1024 * 1024 * 5)
            return RestBean.failure(400,"图像大小最大不能大于5MB");
        log.info("{}正在进行图片上传操作", id);
        String url = imageService.uploadImage(id,file);
        if(url != null){
            log.info("{}图片上传成功，大小：{}", id, file.getSize());
            return RestBean.success(url);
        } else {
            response.setStatus(400);
            return RestBean.failure(400,"图片上传失败，请联系管理员！");
        }
    }
    @PostMapping("/avatar")
    public RestBean<String> uploadAvatar(@RequestAttribute(Const.ATTR_USER_ID) int id,
                                         @RequestParam("file") MultipartFile file) throws Exception {
        if(file.getSize() > 1024 * 200)
            return RestBean.failure(400,"头像图片不能大于200KB");
        log.info("{}正在进行头像上传操作", id);
        String url = imageService.uploadAvatar(id,file);
        if(url != null){
            log.info("{}头像上传成功，大小：{}", id, file.getSize());
            return RestBean.success(url);
        } else {
            return RestBean.failure(400,"头像上传失败，请联系管理员！");
        }
    }

}
