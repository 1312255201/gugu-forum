package cn.gugufish.controller;

import cn.gugufish.entity.RestBean;
import cn.gugufish.service.ImageService;
import io.minio.errors.ErrorResponseException;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 静态资源访问控制器
 * 提供系统中静态资源（主要是图片）的访问服务
 * 作为MinIO对象存储和前端之间的代理层，处理资源的获取和缓存
 * 
 * 主要功能：
 * - 图片资源代理访问：从MinIO获取图片并返回给客户端
 * - HTTP缓存控制：设置合适的缓存头，提高访问性能
 * - 错误处理：处理资源不存在等异常情况
 * 
 * 访问路径模式：/images/**
 * 例如：/images/avatar/123456.jpg 或 /images/cache/abcdef.png
 * 
 * @author GuguFish
 */
@Slf4j
@RestController
public class ObjectController {
    
    /**
     * 图片服务类
     * 提供从MinIO对象存储获取图片资源的功能
     */
    @Resource
    ImageService imageService;
    
    /**
     * 处理图片资源的访问请求
     * 响应所有以 /images/ 开头的图片请求，从MinIO获取对应的图片资源
     * 
     * @param request HTTP请求对象，包含图片路径信息
     * @param response HTTP响应对象，用于返回图片数据和设置响应头
     * @throws Exception 获取图片过程中可能出现的异常
     */
    @GetMapping("/images/**")
    public void imageFetch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 设置响应内容类型为JPEG图片格式
        response.setHeader("Content-Type", "image/jpg");
        this.fetchImage(request, response);
    }
    
    /**
     * 从MinIO获取图片资源的核心方法
     * 解析请求路径，从MinIO对象存储中获取对应的图片文件并返回给客户端
     * 
     * 功能特点：
     * - 路径验证：检查图片路径的有效性（长度必须大于13）
     * - 缓存控制：设置30天的浏览器缓存，提高访问性能
     * - 错误处理：优雅处理文件不存在、服务器错误等异常情况
     * 
     * @param request HTTP请求对象，用于获取图片路径
     * @param response HTTP响应对象，用于输出图片数据和设置响应状态
     * @throws Exception MinIO访问过程中可能出现的异常
     */
    private void fetchImage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 从请求路径中提取图片路径（去掉前缀 "/images"）
        String imagePath = request.getServletPath().substring(7);
        ServletOutputStream stream = response.getOutputStream();
        
        // 验证图片路径的有效性（路径长度必须大于13个字符）
        if(imagePath.length() <= 13){
            response.setStatus(404);
            stream.print(RestBean.failure(404, "Not Found qwq").toString());
        } else {
            try{
                // 从MinIO获取图片数据并写入响应流
                imageService.fetchImageFromMinio(stream, imagePath);
                // 设置缓存控制头：30天缓存（2592000秒）
                response.setHeader("Cache-Control", "max-age=2592000");
            } catch (ErrorResponseException e){
                // 处理MinIO响应错误
                if(e.response().code() == 404){
                    // 图片不存在
                    response.setStatus(404);
                } else {
                    // 其他MinIO错误
                    log.error("从MinIO获取图片出现异常：{}", e.getMessage(), e);
                    response.setStatus(500);
                }
            }
        }
    }
}
