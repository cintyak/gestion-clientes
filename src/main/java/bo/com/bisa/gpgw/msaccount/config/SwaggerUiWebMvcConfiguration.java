package bo.com.bisa.gpgw.msaccount.config;

import bo.com.bisa.gpgw.msaccount.config.apidocs.SwaggerUiWebMvcConfigurer;
import bo.com.bisa.gpgw.msaccount.config.coreprop.CoreConstants;
import bo.com.bisa.gpgw.msaccount.config.coreprop.CoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.util.StopWatch;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.ResponseBuilder;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Configuration
@Profile(CoreConstants.SPRING_PROFILE_API_DOCS)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableSwagger2
public class SwaggerUiWebMvcConfiguration {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    private final CoreProperties coreProperties;

    @Bean
    public Docket api() {
        log.debug("Swagger ApiDocs path: {}, starting", coreProperties.getApiDocs().getDefaultIncludePattern());
        StopWatch watch = new StopWatch();
        watch.start();
        Docket docket = new Docket(DocumentationType.SWAGGER_2).select()
            .paths(PathSelectors.ant(coreProperties.getApiDocs().getDefaultIncludePattern()))
            .build()
            .apiInfo(
                new ApiInfo(
                    coreProperties.getApiDocs().getTitle(),
                    coreProperties.getApiDocs().getDescription(),
                    coreProperties.getApiDocs().getVersion(),
                    coreProperties.getApiDocs().getTermsOfServiceUrl(),
                    new Contact(
                        coreProperties.getApiDocs().getContactName(),
                        coreProperties.getApiDocs().getContactUrl(),
                        coreProperties.getApiDocs().getContactEmail()
                    ),
                    coreProperties.getApiDocs().getLicense(),
                    coreProperties.getApiDocs().getLicenseUrl(),
                    Collections.emptyList()
                )
            )
            .securityContexts(Collections.singletonList(securityContext()))
            .securitySchemes(Collections.singletonList(apiKey()))
            .useDefaultResponseMessages(false)
            .globalResponses(HttpMethod.GET, Arrays.asList(
                new ResponseBuilder().code("500")
                    .description("500 message").build(),
                new ResponseBuilder().code("403")
                    .description("Forbidden!!!!!").build()
            ));
        watch.stop();
        log.debug("Swagger ApiDocs started in {} ms", watch.getTotalTimeMillis());
        return docket;
    }

    private ApiKey apiKey() {
        return new ApiKey("JWT", AUTHORIZATION_HEADER, "header");
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
            .securityReferences(defaultAuth())
            .build();
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return Collections.singletonList(new SecurityReference("JWT", authorizationScopes));
    }

    @Bean
    public SwaggerUiWebMvcConfigurer swaggerUiConfigurer() {
        return new SwaggerUiWebMvcConfigurer();
    }
}
