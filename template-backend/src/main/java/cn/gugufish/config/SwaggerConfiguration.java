package cn.gugufish.config;

import cn.gugufish.entity.RestBean;
import cn.gugufish.entity.vo.response.AuthorizeVO;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Swagger API文档配置类
 * 基于SpringDoc OpenAPI 3规范，为咕咕论坛项目提供API文档服务
 * 主要功能：
 * - 配置API文档基础信息（标题、描述、版本等）
 * - 配置JWT Bearer Token认证方案
 * - 手动添加登录和登出接口的文档定义
 * - 提供在线API测试功能
 * 
 * @author GuguFish
 */
@Configuration
// 配置全局安全认证方案：使用Bearer Token（JWT）
@SecurityScheme(type = SecuritySchemeType.HTTP, scheme = "Bearer",
        name = "Authorization", in = SecuritySchemeIn.HEADER)
// 定义OpenAPI文档需要使用的安全认证
@OpenAPIDefinition(security = { @SecurityRequirement(name = "Authorization") })
public class SwaggerConfiguration {

    /**
     * 配置OpenAPI文档的基础信息
     * 包括项目标题、描述、版本信息、许可证信息和外部文档链接
     * 
     * @return 配置完成的OpenAPI对象
     */
    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                // 配置API文档基础信息
                .info(new Info().title("咕咕论坛 API 文档")
                        .description("欢迎来到咕咕论坛API测试文档，在这里可以快速进行接口调试")
                        .version("1.0")
                        // 配置许可证信息，链接到项目开源地址
                        .license(new License()
                                .name("项目开源地址")
                                .url("https://github.com/1312255201/gugu-forum")
                        )
                )
                // 配置外部文档链接
                .externalDocs(new ExternalDocumentation()
                        .description("我的小窝")
                        .url("https://www.gugufish.cn/")
                );
    }

    /**
     * 配置自定义的OpenAPI扩展
     * 主要用于手动添加一些SpringSecurity自动处理但需要在文档中展示的接口
     * 如登录、登出接口等
     * 
     * @return OpenApiCustomizer自定义配置器
     */
    @Bean
    public OpenApiCustomizer customerGlobalHeaderOpenApiCustomizer() {
        return api -> this.authorizePathItems().forEach(api.getPaths()::addPathItem);
    }

    /**
     * 手动定义认证相关接口的API文档
     * 由于登录和登出接口是由SpringSecurity自动处理的，
     * 不会被Swagger自动扫描到，因此需要手动添加到API文档中
     * 
     * @return 包含认证接口定义的Map集合
     */
    private Map<String, PathItem> authorizePathItems(){
        Map<String, PathItem> map = new HashMap<>();
        
        // 配置登录接口文档
        map.put("/api/auth/login", new PathItem()
                .post(new Operation()
                        // 设置接口分组标签
                        .tags(List.of("登录校验相关"))
                        // 设置接口摘要说明
                        .summary("登录验证接口")
                        // 添加用户名参数
                        .addParametersItem(new QueryParameter()
                                .name("username")
                                .required(true)
                        )
                        // 添加密码参数
                        .addParametersItem(new QueryParameter()
                                .name("password")
                                .required(true)
                        )
                        // 配置响应信息
                        .responses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse()
                                        .description("OK")
                                        // 设置响应示例
                                        .content(new Content().addMediaType("*/*", new MediaType()
                                                .example(RestBean.success(new AuthorizeVO()).asJsonString())
                                        ))
                                )
                        )
                )
        );
        
        // 配置登出接口文档
        map.put("/api/auth/logout", new PathItem()
                .get(new Operation()
                        // 设置接口分组标签
                        .tags(List.of("登录校验相关"))
                        // 设置接口摘要说明
                        .summary("退出登录接口")
                        // 配置响应信息
                        .responses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse()
                                        .description("OK")
                                        // 设置响应示例
                                        .content(new Content().addMediaType("*/*", new MediaType()
                                                .example(RestBean.success())
                                        ))
                                )
                        )
                )

        );
        return map;
    }
}
