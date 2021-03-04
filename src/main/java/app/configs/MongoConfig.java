package app.configs;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import app.controllers.AuthController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableMongoRepositories
public class MongoConfig {

    @Autowired
    private ApplicationConfig config;

    public static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    @Bean
    public MongoClient mongo() throws Exception {
        // for local host
        // final ConnectionString connectionString = new ConnectionString("mongodb://localhost:27017/taskteam");
        // final MongoClientSettings mongoClientSettings = MongoClientSettings.builder().applyConnectionString(connectionString).build();
        // return MongoClients.create(mongoClientSettings);

        return MongoClients.create(config.getMongoUrl());
    }

    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        return new MongoTemplate(mongo(), "taskroom");
    }

}
