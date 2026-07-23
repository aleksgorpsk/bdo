package ag.com.dbo.service;

import ag.com.dbo.models.queue.QueueStorage;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

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
