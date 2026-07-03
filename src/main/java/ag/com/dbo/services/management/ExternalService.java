package ag.com.dbo.services.management;

import ag.com.dbo.controllers.model.ScriptRequest;
import ag.com.dbo.controllers.model.ScriptResponse;
import ag.com.dbo.controllers.model.TaskRequest;
import ag.com.dbo.models.checker.SensorModel;
import ag.com.dbo.models.management.Node;
import ag.com.dbo.models.management.NodeType;
import ag.com.dbo.models.management.StepInstance;
import ag.com.dbo.models.management.statuses.QueueInfo;
import ag.com.dbo.repositories.management.NodeRepository;
import ag.com.dbo.repositories.management.StepInstanceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static ag.com.dbo.utils.Utils.saveError;

@Service
@Slf4j
public class ExternalService implements InitializingBean {

    private final StepInstanceRepository stepInstanceRepository;
    private final RestClient queueRestClient;
    private final RestClient scriptRestClient;
    private final NodeRepository nodeRepository;


    private final Map<Integer, RestClient> nodeMap;

    private List<Node> nodeList = null;
    @Value("${server.port}")
    private String port;

    @Value("${queue.enqueue.path}")
    private String enqueuePath;

    @Value("${script.process.path}")
    private String scriptRunPath;
    @Value("${server.address:localhost}")
    private String serverAddress;

    private Node masterNode; // in case master without Workers

    public ExternalService(StepInstanceRepository stepInstanceRepository,
                           @Qualifier("queueRestClient") RestClient queueRestClient,
                           @Qualifier("scriptRestClient") RestClient scriptRestClient,
                           NodeRepository nodeRepository ,
                           Map<Integer, RestClient> nodeMap)  {
        this.stepInstanceRepository = stepInstanceRepository;
        this.queueRestClient = queueRestClient;
        this.scriptRestClient = scriptRestClient;
        this.nodeRepository = nodeRepository;
        this.nodeMap = nodeMap;
    }


    public void sendToQueue(TaskRequest taskRequest, StepInstance si) throws JsonProcessingException {
        //TODO


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


    public void afterPropertiesSet() throws Exception {
//        this.localHost = serverAddress + ":" + port;
        List<Node> nodes = nodeRepository.findActiveNodeByType(NodeType.Master.name());
        if (nodes.isEmpty()){
            masterNode = null;
        }else {
            masterNode = nodes.get(0);
        }

    }
// UI
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

    public QueueInfo getInfo(Node node) {
        try {
            RestClient client = nodeMap.get(node.getId());
            if (client == null) {
                log.error("Errpr : no restClient in storage ");
                client = RestClient.builder()
                        .baseUrl(node.getHost())
                        .build();
                nodeMap.put(node.getId(), client);
            }

            QueueInfo info = client.get()
                    .uri("/queue/info")
                    .retrieve()
                    .body(QueueInfo.class);
            log.info("Worker Info:{}", info);
            return info;
        } catch (Exception e) {
            log.warn("Host:{} not found! {}", node, e.getMessage());
        }
        return null;
    }

    public RestClient workerRestClient(String host) {
        return RestClient.builder()
                .baseUrl(host) // Your custom server URL
                .defaultHeader("Content-Type", "application/json")
                .build();
    }



    private List<Node> getNodeList() {
        if (nodeList == null) {
            nodeList = nodeRepository.findAll().stream().filter(Node::getActive).toList();
        }
        return nodeList;
    }

    protected List<Node> getWorkerHosts() {
        return getNodeList().stream()
                .filter(Node::getActive)
                .filter(x -> NodeType.Worker.name().equals(x.getType()))
                .toList();
    }

    public Node getHost(String tags ) {

        if (getNodeList() == null) { // no correct config
            return masterNode;
        }
        List<Node> workerNodes = new ArrayList<>(getWorkerHosts());
        if (workerNodes.isEmpty()) { // no workers
            return masterNode;
        }
        /*
        if (freeNodes.size() == 1) {
            return freeNodes.get(0);
        }
*/
        List<Node>freeTaggedNodes = checkTags(workerNodes, tags);
        if (freeTaggedNodes.isEmpty()){
            log.warn("No Workers with correct tag");
            return null;
        }
        if (freeTaggedNodes.size() == 1){
            log.info(" Only one node. No competition.");
            return null;
        }

        if (freeTaggedNodes.stream().filter(x -> x.getFreeSlots() > 0).toList().isEmpty()) {
            freeTaggedNodes.sort(Comparator.comparing(Node::getFreeSlots).reversed());
            return freeTaggedNodes.get(0);
        } else {
            log.warn("No free workers!!!");
            return freeTaggedNodes.get(ThreadLocalRandom.current().nextInt(freeTaggedNodes.size()));
        }
    }

    private List<String> splitString(String s){
        if (s==null){
            return Collections.emptyList();
        }
        return Arrays.stream(s.split(",")).map(String::trim).toList();
    }

    private boolean nodeContainsTag(Node node,  List<String>requiredTagsList){
        if(StringUtils.isEmpty(node.getTags())){
            return true;
        }
        List<String> tags = splitString(node.getTags());
        for(String tag: requiredTagsList){
            if( tags.contains(tag)){
                return true;
            }
        }
        return  false;
    }

    private  List<Node> checkTags(List<Node> nodes, String requiredTags ){
        if(StringUtils.isEmpty(requiredTags)){
            return nodes;
        }

        List<String> requiredTagsList = Stream.of(requiredTags.split(",")).map(String::trim).toList();
        List<Node> result = new ArrayList<>();
        for (Node node :nodes){
            if (nodeContainsTag(node, requiredTagsList)){
                result.add(node);
            }
        }
      return result;
    }
}
