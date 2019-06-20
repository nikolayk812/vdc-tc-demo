package demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@SpringBootTest
@ContextConfiguration(initializers = {ItemControllerTest.PropertiesInitializer.class})
@Testcontainers
@ActiveProfiles("test")
public class ItemControllerTest {

    @Container
    static GenericContainer redis = new GenericContainer("redis:5.0.5")
            .withExposedPorts(6379);

    @Autowired
    private ItemApplication.ItemController controller;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void init() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void testCreateItem() {
        String userId = UUID.randomUUID().toString();
        mockUser(userId);

        ItemCreateResponse response = controller.create(new Item("", "T-shirt", "XL", userId));
        assertThat(response.getId(), not(isEmptyString()));

        ItemResponse item = controller.get(response.getId());
        assertThat(item.getCategory(), equalTo("T-shirt"));
        assertThat(item.getUser().getName(), equalTo("Nikolay"));
    }

    @SneakyThrows
    private void mockUser(String id) {
        User user = new User(id, "Nikolay", "nikolay@testcontainers.org");
        mockServer.expect(ExpectedCount.once(),
                requestTo(new URI("http://user-eureka/users/" + id)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(new ObjectMapper().writeValueAsString(user))
                );
    }

    static class PropertiesInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.redis.host=localhost",
                    "spring.redis.port=" + redis.getFirstMappedPort(),
                    "eureka.client.enabled=false"
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

}