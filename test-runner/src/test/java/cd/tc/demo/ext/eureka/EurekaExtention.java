package cd.tc.demo.ext.eureka;

import cd.tc.demo.ext.NetworkHolder;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class EurekaExtention implements BeforeAllCallback, AfterAllCallback {
    private EurekaContainer eurekaContainer = new EurekaContainer(NetworkHolder.network(), 8761);

    @Override
    public void beforeAll(ExtensionContext context) {
        eurekaContainer.start();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        eurekaContainer.stop();
    }


}
