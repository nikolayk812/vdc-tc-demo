package cd.tc.demo.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;

@SpringBootTest
@ContextConfiguration(initializers = {UserControllerTest.PropertiesInitializer.class})
@Testcontainers
public class UserControllerTest {

    @Autowired
    private UserApplication.UserController controller;

    @Container
    private static final PostgreSQLContainer postgres = new PostgreSQLContainer()
            .withDatabaseName("users")
            .withUsername("postgres")
            .withPassword("password");


    @Test
    public void testCreateGetUser() {
        UserCreateResponse response = controller.create(
                new User("", "Nikolay", "nikolay@testcontainers.org"));
        assertThat(response.getId(), not(isEmptyString()));

        User user = controller.get(response.getId());
        assertThat(user.getName(), equalTo("Nikolay"));
    }

    static class PropertiesInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgres.getJdbcUrl(),
                    "spring.datasource.username=" + postgres.getUsername(),
                    "spring.datasource.password=" + postgres.getPassword(),
                    "eureka.client.enabled=false"
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

}