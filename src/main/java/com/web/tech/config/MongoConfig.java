package com.web.tech.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableMongoRepositories(basePackages = "com.web.tech.repository")
public class MongoConfig extends AbstractMongoClientConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MongoConfig.class);

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    @Bean
    @Override
    public MongoClient mongoClient() {
        try {
            logger.info("Configuring MongoDB client for database: {}", databaseName);

            ConnectionString connectionString = new ConnectionString(mongoUri);

            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .serverApi(ServerApi.builder()
                            .version(ServerApiVersion.V1)
                            .build())
                    // Optimized connection pool settings for Heroku/Atlas
                    .applyToConnectionPoolSettings(builder -> {
                        builder.maxConnectionIdleTime(30, TimeUnit.SECONDS)
                                .maxConnectionLifeTime(60, TimeUnit.SECONDS)
                                .minSize(1)
                                .maxSize(10)
                                .maxWaitTime(10, TimeUnit.SECONDS);
                        logger.debug("Connection pool configured: min=1, max=10, idle=30s, lifetime=60s");
                    })
                    // Socket timeout settings
                    .applyToSocketSettings(builder -> {
                        builder.connectTimeout(10, TimeUnit.SECONDS)
                                .readTimeout(15, TimeUnit.SECONDS);
                        logger.debug("Socket timeouts configured: connect=10s, read=15s");
                    })
                    // Server settings for better reliability
                    .applyToServerSettings(builder -> {
                        builder.heartbeatFrequency(5, TimeUnit.SECONDS)
                                .minHeartbeatFrequency(1, TimeUnit.SECONDS);
                        logger.debug("Server settings configured: heartbeat=5s, min-heartbeat=1s");
                    })
                    // Cluster settings for server selection timeout
                    .applyToClusterSettings(builder -> {
                        builder.serverSelectionTimeout(10, TimeUnit.SECONDS);
                        logger.debug("Cluster settings configured: server-selection-timeout=10s");
                    })
                    .build();

            MongoClient client = MongoClients.create(settings);
            logger.info("MongoDB client created successfully");
            return client;

        } catch (Exception e) {
            logger.error("Failed to create MongoDB client: {}", e.getMessage(), e);
            throw new RuntimeException("MongoDB configuration failed", e);
        }
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        try {
            MongoTemplate template = new MongoTemplate(mongoClient(), getDatabaseName());
            logger.info("MongoTemplate created successfully for database: {}", getDatabaseName());

            // Test the connection
            template.getCollection("clients").estimatedDocumentCount();
            logger.info("MongoDB connection test successful");

            return template;
        } catch (Exception e) {
            logger.error("Failed to create MongoTemplate or test connection: {}", e.getMessage(), e);
            throw new RuntimeException("MongoDB template configuration failed", e);
        }
    }

    // Bean for database connection health check
    @Bean
    public MongoHealthIndicator mongoHealthIndicator() {
        return new MongoHealthIndicator(mongoTemplate());
    }

    // Custom health indicator class
    public static class MongoHealthIndicator {
        private final MongoTemplate mongoTemplate;
        private static final Logger logger = LoggerFactory.getLogger(MongoHealthIndicator.class);

        public MongoHealthIndicator(MongoTemplate mongoTemplate) {
            this.mongoTemplate = mongoTemplate;
        }

        public boolean isHealthy() {
            try {
                mongoTemplate.getCollection("clients").estimatedDocumentCount();
                logger.debug("MongoDB health check passed");
                return true;
            } catch (Exception e) {
                logger.warn("MongoDB health check failed: {}", e.getMessage());
                return false;
            }
        }
    }
}