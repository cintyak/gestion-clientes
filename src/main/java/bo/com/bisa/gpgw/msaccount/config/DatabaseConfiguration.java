package bo.com.bisa.gpgw.msaccount.config;

import bo.com.bisa.gpgw.msaccount.config.support.PgwSimpleJpaRepository;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "bo.com.bisa.gpgw.msaccount.repository", repositoryBaseClass = PgwSimpleJpaRepository.class)
@EntityScan("bo.com.bisa.gpgw.domain")
@EnableJpaAuditing(auditorAwareRef = "springSecurityAuditorAware")
@EnableTransactionManagement
public class DatabaseConfiguration {
}
