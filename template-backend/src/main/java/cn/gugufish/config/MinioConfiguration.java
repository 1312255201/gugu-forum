package cn.gugufish.config;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * Minio对象存储配置配置
 */
@Slf4j
@Configuration
public class MinioConfiguration {
    @Value("${spring.minio.endpoint}")
    String endpoint;
    @Value("${spring.minio.username}")
    String username;
    @Value("${spring.minio.password}")
    String password;
    @Bean
    public MinioClient minioClient(){
        log.info("MinioClient初始化中");
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(username,password)
                .build();
    }
}
