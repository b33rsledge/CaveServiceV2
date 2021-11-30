package dk.msdo.repository;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.GenericContainer;

@SpringBootTest
public class TestContainerRedisDB {

        private static final int REDIS_PORT = 6379;

        public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

            static GenericContainer redis = new GenericContainer<>("redis:6-alpine")
                    .withExposedPorts(REDIS_PORT)
                    .withReuse(true);

            @Override
            public void initialize(ConfigurableApplicationContext context) {
                // Start container
                redis.start();

                // Override Redis configuration
                String redisContainerIP = "spring.redis.host=" + redis.getContainerIpAddress();
                String redisContainerPort = "spring.redis.port=" + redis.getMappedPort(REDIS_PORT); // <- This is how you get the random port.
                TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,  redisContainerIP, redisContainerPort); // <- This is how you override the configuration in runtime.
            }
        }
    }

