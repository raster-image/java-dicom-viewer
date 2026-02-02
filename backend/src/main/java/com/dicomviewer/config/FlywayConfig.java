package com.dicomviewer.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Flyway configuration to ensure migrations run before JPA initialization.
 */
@Configuration
public class FlywayConfig {

    private static final Logger log = LoggerFactory.getLogger(FlywayConfig.class);

    @Bean
    public Flyway flyway(DataSource dataSource) {
        log.info("Configuring Flyway for database migrations...");
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .load();

        // Execute migration
        log.info("Running Flyway migrations...");
        flyway.migrate();
        log.info("Flyway migrations completed.");

        return flyway;
    }

    /**
     * Ensure EntityManagerFactory depends on Flyway bean, so migrations run first.
     */
    @Bean
    public static BeanFactoryPostProcessor flywayDependencyPostProcessor() {
        return beanFactory -> {
            String[] jpaRelatedBeans = {"entityManagerFactory", "transactionManager"};
            for (String beanName : jpaRelatedBeans) {
                if (beanFactory.containsBeanDefinition(beanName)) {
                    var beanDefinition = beanFactory.getBeanDefinition(beanName);
                    String[] existingDependsOn = beanDefinition.getDependsOn();
                    String[] newDependsOn;
                    if (existingDependsOn == null) {
                        newDependsOn = new String[]{"flyway"};
                    } else {
                        newDependsOn = new String[existingDependsOn.length + 1];
                        System.arraycopy(existingDependsOn, 0, newDependsOn, 0, existingDependsOn.length);
                        newDependsOn[existingDependsOn.length] = "flyway";
                    }
                    beanDefinition.setDependsOn(newDependsOn);
                }
            }
        };
    }
}
