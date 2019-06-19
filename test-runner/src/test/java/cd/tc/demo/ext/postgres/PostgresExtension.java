package cd.tc.demo.ext.postgres;

import cd.tc.demo.ext.NetworkHolder;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresExtension implements BeforeAllCallback, AfterAllCallback {
    private final GenericContainer container = new PostgreSQLContainer()
            .withDatabaseName("users")
            .withUsername("postgres")
            .withPassword("password")
            .withNetworkAliases("postgres")
            .withNetwork(NetworkHolder.network());

    @Override
    public void afterAll(ExtensionContext context) {
        container.stop();
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        container.start();
    }
}
