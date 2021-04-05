package app.configs;


import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

import app.controllers.AuthController;

@Configuration
@EnableReactiveMongoRepositories
public class ReactiveMongoConfig {

    public static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    @Bean
    public MongoClient mongoClient() throws Exception {
        // for local host mongoDB
        final ConnectionString connectionString = new ConnectionString("mongodb://localhost:27017/taskteam");
        final MongoClientSettings mongoClientSettings = MongoClientSettings.builder().applyConnectionString(connectionString).build();
        return MongoClients.create(mongoClientSettings);

        //for cloud mongoDb
        // return MongoClients.create("mongodb+srv://taskUser:iamyuri@taskroom.mzeo2.mongodb.net/taskroom?retryWrites=true&w=majority");
    }

    @Bean
    public ReactiveMongoTemplate reactiveMongoTemplate() throws Exception {
        return new ReactiveMongoTemplate(mongoClient(), "taskroom");
    }

}
