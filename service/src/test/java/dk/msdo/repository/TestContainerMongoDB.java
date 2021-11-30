package dk.msdo.repository;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.GenericContainer;

@SpringBootTest
public class TestContainerMongoDB {

        private static final int MONGO_PORT = 27017;

        public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

            static GenericContainer mongo  = new GenericContainer<>("mongo")
                    .withExposedPorts(MONGO_PORT)
                    .withReuse(true);

            @Override
            public void initialize(ConfigurableApplicationContext context) {
                // Start container
                mongo.start();

                // Override Redis configuration
                String mongoIP = "spring.data.mongodb.host=" + mongo.getContainerIpAddress();
                String mongoPort = "spring.data.mongodb.port=" + mongo.getMappedPort(MONGO_PORT); // <- This is how you get the random port.
                TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,  mongoIP, mongoPort); // <- This is how you override the configuration in runtime.
            }
        }
    }

