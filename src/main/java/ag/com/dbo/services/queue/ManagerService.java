package ag.com.dbo.services.queue;

import ag.com.dbo.models.queue.QueueStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Service
public class ManagerService {
    @Value("${spring.manager.return.url}")
    private String url;

    private final RestClient restClient = RestClient.create("http://localhost:9000");

    public void sendResult(QueueStorage result){
        log.info("send: {}", result);
        this.restClient.put().uri("/return/task")
                .contentType(MediaType.APPLICATION_JSON)
                .body(result)
                .retrieve()
                .toBodilessEntity();
    }
}
