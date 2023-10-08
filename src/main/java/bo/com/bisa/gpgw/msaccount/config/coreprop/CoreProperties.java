package bo.com.bisa.gpgw.msaccount.config.coreprop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.web.cors.CorsConfiguration;

@ConfigurationProperties(prefix = "coreprop", ignoreUnknownFields = false)
@PropertySources({
    @PropertySource(value = "classpath:git.properties", ignoreResourceNotFound = true),
    @PropertySource(value = "classpath:META-INF/build-info.properties", ignoreResourceNotFound = true)
})
@Getter
public class CoreProperties {

    private final Security security = new Security();
    private final CorsConfiguration cors = new CorsConfiguration();
    private final ApiDocs apiDocs = new ApiDocs();

    @Setter
    @Getter
    public static class Security {

        private String contentSecurityPolicy = CoreDefaults.Security.contentSecurityPolicy;
        private final Authentication authentication = new Authentication();
        private final RememberMe rememberMe = new RememberMe();

        @Getter
        public static class Authentication {

            private final Jwt jwt = new Jwt();

            @Setter
            @Getter
            public static class Jwt {

                private String secret = CoreDefaults.Security.Authentication.Jwt.secret;
                private String base64Secret = CoreDefaults.Security.Authentication.Jwt.base64Secret;
                private long tokenValidityInSeconds = CoreDefaults.Security.Authentication.Jwt.tokenValidityInSeconds;
                private long tokenValidityInSecondsForRememberMe = CoreDefaults.Security.Authentication.Jwt.tokenValidityInSecondsForRememberMe;
            }
        }

        @Setter
        @Getter
        public static class RememberMe {

            private String key = CoreDefaults.Security.RememberMe.key;
        }
    }

    @Setter
    @Getter
    public static class ApiDocs {

        private String title = CoreDefaults.ApiDocs.title;

        private String description = CoreDefaults.ApiDocs.description;

        private String version = CoreDefaults.ApiDocs.version;

        private String termsOfServiceUrl = CoreDefaults.ApiDocs.termsOfServiceUrl;

        private String contactName = CoreDefaults.ApiDocs.contactName;

        private String contactUrl = CoreDefaults.ApiDocs.contactUrl;

        private String contactEmail = CoreDefaults.ApiDocs.contactEmail;

        private String license = CoreDefaults.ApiDocs.license;

        private String licenseUrl = CoreDefaults.ApiDocs.licenseUrl;

        private String defaultIncludePattern = CoreDefaults.ApiDocs.defaultIncludePattern;
    }
}
