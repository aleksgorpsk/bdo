package ag.com.dbo.services.management;

import ag.com.dbo.controllers.model.ScriptRequest;
import ag.com.dbo.controllers.model.ScriptResponse;
import ag.com.dbo.controllers.model.TaskRequest;
import ag.com.dbo.models.checker.SensorModel;
import ag.com.dbo.models.management.StepInstance;
import ag.com.dbo.models.management.statuses.QueueInfo;
import ag.com.dbo.repositories.management.StepInstanceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import static ag.com.dbo.utils.Utils.saveError;

@Service
@Slf4j
public class ExternalService {

    private final StepInstanceRepository stepInstanceRepository;
    private final RestClient queueRestClient;
    private final RestClient scriptRestClient;

    @Value("${queue.enqueue.path}")
    private String enqueuePath;

    @Value("${script.process.path}")
    private String scriptRunPath;

    public ExternalService(StepInstanceRepository stepInstanceRepository,
                           @Qualifier("queueRestClient") RestClient queueRestClient,
                           @Qualifier("scriptRestClient") RestClient scriptRestClient) {
        this.stepInstanceRepository = stepInstanceRepository;
        this.queueRestClient = queueRestClient;
        this.scriptRestClient = scriptRestClient;
    }


    public void sendToQueue(TaskRequest taskRequest, StepInstance si) throws JsonProcessingException {
        try {
            this.queueRestClient.put().uri(enqueuePath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(taskRequest)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Throwable e) {
            saveError(si, stepInstanceRepository, e, "cannot send to queue");

        }
    }

    /**
     * script Ok/Not
     *
     * @param si
     * @param sModel
     * @return
     * @throws JsonProcessingException
     */
    public ScriptResponse sendSensorScript(StepInstance si, SensorModel sModel) throws JsonProcessingException {
        ScriptRequest scriptRequest = sModel.scriptRequest();
        scriptRequest.setParams(si.getVars());
        scriptRequest.setResults(si.getEtlInstance().getEtlVars());
        scriptRequest.setParams(si.getVars());
        scriptRequest.setStepName(si.getName());

        try {
            return this.scriptRestClient.put().uri(scriptRunPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(scriptRequest)
                    .retrieve()
                    .body(ScriptResponse.class);

        } catch (Throwable e) {
            saveError(si, stepInstanceRepository, e, "cannot send to sensor");
            return new ScriptResponse("Error", e.getMessage());
        }
    }

    public QueueInfo getInfo(String host) {
try {
    return workerRestClient(host).get()
            .uri("/queue/info")
            .retrieve()
            .body(QueueInfo.class);
}catch (Exception e){
    log.warn("Host:{} not found! {}", host,e.getMessage());
}
return null;
    }

    public RestClient workerRestClient(String host) {
        return RestClient.builder()
                .baseUrl(host) // Your custom server URL
                .defaultHeader("Content-Type", "application/json")
                .build();
    }


}
