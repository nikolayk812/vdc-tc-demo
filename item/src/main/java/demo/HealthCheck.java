package demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import java.util.List;

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