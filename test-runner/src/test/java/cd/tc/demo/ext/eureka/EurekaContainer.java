package cd.tc.demo.ext.eureka;

import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;

public class EurekaContainer extends GenericContainer<EurekaContainer> {
    public EurekaContainer(Network network, int port) {
        super("tc-demo/eureka:latest");
        addExposedPorts(port);
        withNetwork(network);
        withNetworkAliases("eureka");
        withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(" --- eureka --- ")));
        waitingFor(new HostPortWaitStrategy());
    }
}
