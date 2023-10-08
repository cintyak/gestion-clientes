package bo.com.bisa.gpgw.msaccount.config.liquibase;

import bo.com.bisa.gpgw.msaccount.config.coreprop.CoreConstants;
import liquibase.exception.LiquibaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.liquibase.DataSourceClosingSpringLiquibase;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.util.StopWatch;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Executor;

public class AsyncSpringLiquibase extends DataSourceClosingSpringLiquibase {

    public static final String DISABLED_MESSAGE = "Liquibase is disabled";
    public static final String STARTING_ASYNC_MESSAGE = "Starting Liquibase asynchronously, your database might not be ready at startup!";
    public static final String STARTING_SYNC_MESSAGE = "Starting Liquibase synchronously";
    public static final String STARTED_MESSAGE = "Liquibase has updated your database in {} ms";
    public static final String EXCEPTION_MESSAGE = "Liquibase could not start correctly, your database is NOT ready: " + "{}";

    public static final long SLOWNESS_THRESHOLD = 5; // seconds
    public static final String SLOWNESS_MESSAGE = "Warning, Liquibase took more than {} seconds to start up!";

    private final Logger logger = LoggerFactory.getLogger(AsyncSpringLiquibase.class);

    private final Executor executor;

    private final Environment env;

    public AsyncSpringLiquibase(Executor executor, Environment env) {
        this.executor = executor;
        this.env = env;
    }

    @Override
    public void afterPropertiesSet() throws LiquibaseException {
        if (!env.acceptsProfiles(Profiles.of(CoreConstants.SPRING_PROFILE_NO_LIQUIBASE))) {
            if (env.acceptsProfiles(Profiles.of(CoreConstants.SPRING_PROFILE_DEVELOPMENT))) {
                // Prevent Thread Lock with spring-cloud-context GenericScope
                // https://github.com/spring-cloud/spring-cloud-commons/commit/aaa7288bae3bb4d6fdbef1041691223238d77b7b#diff-afa0715eafc2b0154475fe672dab70e4R328
                try (Connection connection = getDataSource().getConnection()) {
                    executor.execute(() -> {
                        try {
                            logger.warn(STARTING_ASYNC_MESSAGE);
                            initDb();
                        } catch (LiquibaseException e) {
                            logger.error(EXCEPTION_MESSAGE, e.getMessage(), e);
                        }
                    });
                } catch (SQLException e) {
                    logger.error(EXCEPTION_MESSAGE, e.getMessage(), e);
                }
            } else {
                logger.debug(STARTING_SYNC_MESSAGE);
                initDb();
            }
        } else {
            logger.debug(DISABLED_MESSAGE);
        }
    }

    protected void initDb() throws LiquibaseException {
        StopWatch watch = new StopWatch();
        watch.start();
        super.afterPropertiesSet();
        watch.stop();
        logger.debug(STARTED_MESSAGE, watch.getTotalTimeMillis());
        if (watch.getTotalTimeMillis() > SLOWNESS_THRESHOLD * 1000L) {
            logger.warn(SLOWNESS_MESSAGE, SLOWNESS_THRESHOLD);
        }
    }
}
