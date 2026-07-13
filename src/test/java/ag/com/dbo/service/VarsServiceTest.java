package ag.com.dbo.service;

import ag.com.dbo.models.management.Node;
import ag.com.dbo.models.queue.QueueStorage;
import ag.com.dbo.repositories.management.NodeRepository;
import ag.com.dbo.services.management.ExternalService;
import ag.com.dbo.services.management.NodeService;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//@ExtendWith(MockitoExtension.class) // Initializes Mockito annotations automatically
@SpringBootTest
@ActiveProfiles("test")

public class VarsServiceTest {


    @Test
    public void addresultTest() {
        // Arrange
        QueueStorage storage = new QueueStorage();
        storage.setName("testData");
        assertDoesNotThrow(() -> {
            String result = storage.addResultToLocalVar("result","vvvvvvsssssss");
            assertEquals("{\"result\":\"vvvvvvsssssss\"}", result);
        });
    }
}
