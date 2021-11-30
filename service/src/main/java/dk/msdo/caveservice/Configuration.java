package dk.msdo.caveservice;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dk.msdo.caveservice.domain.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.text.SimpleDateFormat;
import java.util.Objects;

@org.springframework.context.annotation.Configuration
public class Configuration {

    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);
    @Autowired
    Environment environment;

    /*
        RedisDB Configuration
     */
    @Bean
    @ConditionalOnProperty(value = "storage.room", havingValue = "redisStorage")
    public RedisConnectionFactory connectionFactory() {
        RedisStandaloneConfiguration redisConfiguration = new RedisStandaloneConfiguration();
        redisConfiguration.setHostName(Objects.requireNonNull(environment.getProperty("spring.redis.host")));
        redisConfiguration.setPort(Integer.parseInt(Objects.requireNonNull(environment.getProperty("spring.redis.port"))));
        return new LettuceConnectionFactory(redisConfiguration);
    }

    /**
     * Load Redis room repository if redisStorage is in active profiles
     * <p>
     * Spring boot as default considers Redis DB as a CacheManager. This means the default is to use CacheManager
     * serialization meaning that keys in Redis DB will include class/method signatures, which we do not want. Hence
     * we must setup our own serialization.
     */
    @Bean
    @ConditionalOnProperty(value = "storage.room", havingValue = "redisStorage")
    public RedisTemplate<String, Room> roomTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Room> roomTemplate = new RedisTemplate<>();
        roomTemplate.setConnectionFactory(connectionFactory);

        // Construct the serializer
        //Turn on the default type
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        //Set date format

        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        Jackson2JsonRedisSerializer<? extends Room> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Room.class);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

        // Attach serializer to the template to avoid caching behaviour for key, value sets.
        roomTemplate.setKeySerializer(new StringRedisSerializer());
        roomTemplate.setValueSerializer(jackson2JsonRedisSerializer);

        // Attach serializer to the template to avoid caching behaviour for Hash sets.
        roomTemplate.setHashKeySerializer(new StringRedisSerializer());
        roomTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);

        return roomTemplate;
    }

    /*
        MongoDB Configuration
     */
    @Bean
    @ConditionalOnProperty(value = "storage.room", havingValue = "mongoStorage")
    public MongoClient mongo() {

        ConnectionString connectionString = new ConnectionString(new StringBuilder().append("mongodb://")
                .append(environment.getProperty("spring.data.mongodb.host"))
                .append(":")
                .append(environment.getProperty("spring.data.mongodb.port"))
                .append("/")
                .append(environment.getProperty("spring.data.mongodb.database")).toString());

        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();


        return MongoClients.create(mongoClientSettings);
    }

    @Bean
    @ConditionalOnProperty(value = "storage.room", havingValue = "mongoStorage")
    public MongoTemplate mongoTemplate() throws Exception {
        return new MongoTemplate(mongo(), Objects.requireNonNull(environment.getProperty("spring.data.mongodb.database")));
    }
}