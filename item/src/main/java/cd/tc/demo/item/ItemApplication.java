package cd.tc.demo.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@SpringBootApplication
public class ItemApplication {
    public static void main(String[] args) {
        SpringApplication.run(ItemApplication.class, args);
    }

    @Profile("default")
    @LoadBalanced
    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @RestController
    @RequestMapping(value = "items")
    class ItemController {
        private final ConcurrentMap<String, Item> store = new ConcurrentHashMap<>();

        @Autowired
        private RestTemplate restTemplate;

        @Autowired
        private RedisCommands<String, String> redisCommands;

        @PostMapping
        @SneakyThrows
        public ItemCreateResponse create(@RequestBody Item request) {
            ResponseEntity<User> response = restTemplate.getForEntity(
                    "http://user-eureka/users/{id}", User.class, request.getUserId());

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new IllegalArgumentException("User does not exist " + request.getUserId());
            }

            redisCommands.set("user:" + request.getUserId(), objectMapper().writeValueAsString(response.getBody()));

            String id = UUID.randomUUID().toString();
            Item item = new Item(id, request.getCategory(), request.getDesc(), request.getUserId());
            store.put(id, item);

            return new ItemCreateResponse(id);
        }

        @GetMapping("/{id}")
        @SneakyThrows
        public ItemResponse get(@PathVariable String id) {
            Item item = store.get(id);
            if (item == null) {
                throw new IllegalStateException("Item does not exist " + id);
            }

            String userJson = redisCommands.get("user:" + item.getUserId());
            User user = objectMapper().readValue(userJson, User.class);
            return new ItemResponse(id, item.getCategory(), item.getDesc(), user);
        }
    }

    @Bean
    public RedisCommands<String, String> redisCommands(RedisProperties properties) {
        RedisClient redisClient = RedisClient.create("redis://" + properties.getHost() + ":" + properties.getPort());
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        return connection.sync();
    }

    @Data
    @Component
    @ConfigurationProperties(prefix = "spring.redis")
    class RedisProperties {
        String host;
        String port;
    }

    @Profile("test")
    @Bean
    public RestTemplate getTestRestTemplate() {
        return new RestTemplate();
    }

}

@Value
class Item {
    String id;
    String category;
    String desc;
    String userId;
}


@Value
class ItemCreateResponse {
    String id;
}

@Value
class ItemResponse {
    String id;
    String category;
    String desc;
    User user;
}


@Value
class User {
    String id;
    String name;
    String email;
}

@Component
class HealthCheck implements HealthIndicator {
    @Autowired
    private DiscoveryClient discoveryClient;

    @Override
    public Health health() {
        List<ServiceInstance> userInstances = discoveryClient.getInstances("user-eureka");
        if (!userInstances.isEmpty()) {
            return Health.up().build();
        }
        return Health.down().withDetail("reason", "No user instances").build();
    }
}