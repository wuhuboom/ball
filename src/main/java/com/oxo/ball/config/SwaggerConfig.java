package com.oxo.ball.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableSwagger2
@ConditionalOnProperty(prefix = "some-comfig", value = {"api-bookmaker"}, havingValue = "1")
public class SwaggerConfig {
    @Autowired
    SomeConfig someConfig;
    @Bean
    public Docket createRestApi() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .pathMapping("/")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.oxo.ball.controller.player"))
                .paths(PathSelectors.any())
                .build().apiInfo(new ApiInfoBuilder()
                        .title("在线测试+文档")
                        .description(
                                "登录成功后点击右边[Authorize]按钮填写拿到的token" +
                                        "<br>全局响应CODE说明,其它CODE请参考接口说明" +
                                        "<br>200-正确的响应" +
                                        "<br>401-无权访问" +
                                        "<br>402-未登录或者登录失效" +
                                        "<br>403-账号已被禁用" +
                                        "<br>409-操作频繁" +
                                        "<br>410-该地区访问受权" +
                                        "<br>500-出错的响应" +
                                        "<br>103-表单提交项验证未通过"
                        )
                        .build())
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts());
        return docket;
    }

    private List<SecurityContext> securityContexts() {
        List<SecurityContext> securityContextList = new ArrayList<>();
        List<SecurityReference> securityReferenceList = new ArrayList<>();
        securityReferenceList.add(new SecurityReference("token", scopes()));
        securityContextList.add(SecurityContext
                .builder()
                .securityReferences(securityReferenceList)
                .forPaths(PathSelectors.any())
                .build()
        );
        return securityContextList;
    }

    private AuthorizationScope[] scopes() {
        return new AuthorizationScope[]{new AuthorizationScope("global", "accessAnything")};
    }

    private List<ApiKey> securitySchemes() {
        List<ApiKey> apiKeyList = new ArrayList<>();
        apiKeyList.add(new ApiKey("token", "token", "header"));
        return apiKeyList;
    }

//    private List<Parameter> setHeaderToken() {
//        ParameterBuilder tokenPar = new ParameterBuilder();
//        List<Parameter> pars = new ArrayList<>();
//        tokenPar.name("token").description("token").modelRef(new ModelRef("string")).parameterType("header").required(false).build();
//        pars.add(tokenPar.build());
//        return pars;
//    }

//    /**
//     * 解决swagger-ui.html 404无法访问的问题
//     */
//    @Override
//    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
//        // 解决swagger无法访问
//        registry.addResourceHandler("/swagger-ui.html")
//                .addResourceLocations("classpath:/META-INF/resources/");
//        // 解决swagger的js文件无法访问
//        registry.addResourceHandler("/webjars/**")
//                .addResourceLocations("classpath:/META-INF/resources/webjars/");
//
//    }
}
