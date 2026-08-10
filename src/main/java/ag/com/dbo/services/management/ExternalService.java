package ag.com.dbo.services.management;

import ag.com.dbo.controllers.model.ScriptRequest;

import ag.com.dbo.controllers.model.ScriptResponse;
import ag.com.dbo.controllers.queue.QueueStatus;
import ag.com.dbo.models.management.Etl;
import ag.com.dbo.models.management.Node;
import ag.com.dbo.models.management.NodeType;
import ag.com.dbo.models.management.StepInstance;
import ag.com.dbo.models.management.statuses.QueueInfo;
import ag.com.dbo.models.queue.QueueStorage;
import ag.com.dbo.models.script.Script;
import ag.com.dbo.models.script.ScriptDefinition;
import ag.com.dbo.repositories.management.NodeRepository;
import ag.com.dbo.repositories.management.ScriptRepository;
import ag.com.dbo.repositories.management.StepInstanceRepository;
import ag.com.dbo.services.queue.model.PropData;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.InitializingBean;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigInteger;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;
import ag.com.scheduling.models.ScheduleData;

import static ag.com.dbo.services.Utils.getScriptDefinition;
import static ag.com.dbo.services.queue.utils.QueueConstants.UNKNOWN_ERROR;
import static ag.com.dbo.utils.Utils.saveError;

@Service
@Slf4j
public class ExternalService implements InitializingBean {

    private final StepInstanceRepository stepInstanceRepository;
    private final NodeRepository nodeRepository;
    private final ScriptRepository scriptRepository;


    public Map<Integer, RestClient> nodeMap;

    private List<Node> nodeList = null;
    @Value("${server.port}")
    public String port;

    @Value("${queue.enqueue.path}")
    private String enqueuePath;

    @Qualifier("scheduleRestClient")
    private RestClient scheduleRestClient;

    @Value("${spring.manager.return.path}")
    private String returnTpManagerPath;

    @Value("${schedule.delete.path}")
    private String deleteSchedule;

    @Value("${schedule.update.path}")
    private String updateSchedule;

    public Node masterNode; // in case master without Workers


    public ExternalService(StepInstanceRepository stepInstanceRepository,
                           NodeRepository nodeRepository, ScriptRepository scriptRepository) {
        this.stepInstanceRepository = stepInstanceRepository;
        this.nodeRepository = nodeRepository;

        this.scriptRepository = scriptRepository;
    }

    public boolean  prepareToQueue(StepInstance si, ScriptDefinition scriptDefinition) {
        log.info("-------!!!!!!startStep: {}", si);
        try {
            boolean send = sendToQueue(si, scriptDefinition);
            log.info("sent to queue:{} {}", si.getStepInstanceId(), send);
            return send;
        } catch (Throwable e) {
            saveError(si, stepInstanceRepository, e, "cannot send to queue");
        }
        return false;
    }

    public void sendToManager(QueueStorage result) throws JsonProcessingException {
        RestClient client = getManager();
        client.put().uri(returnTpManagerPath)
                .contentType(MediaType.APPLICATION_JSON)
                .body(result)
                .retrieve()
                .toBodilessEntity();
    }

    public RestClient getManager(){
        List<Node> nodes = nodeRepository.findAll().stream()
                .filter(Node::getActive)
                .filter(x-> NodeType.Master.name().equals( x.getType()))
                .toList();
        if (nodes.isEmpty()) { // if one node
            String host= "http://localhost:" + port;
            RestClient client = RestClient.builder()
                    .baseUrl(host)
                    .build();
            masterNode = new Node(1, "Master", "http://localhost:" + port,
                    NodeType.Master.name(), "", true, 10, 0);
            Node newMasterNode=nodeRepository.saveAndFlush(masterNode);
            nodeMap.put(newMasterNode.getId(), client);
            this.masterNode = newMasterNode;
            return client;
        } else if (nodes.size()==1) {
             return  nodeMap.get(nodes.get(0).getId());
        }else{
            return nodeMap.get( nodes.get(ThreadLocalRandom.current().nextInt(nodes.size())).getId());
        }

    }


    public Map<Integer, RestClient> getNodeClients() {
        List<Node> nodes = nodeRepository.findAll().stream().filter(Node::getActive).toList();
        Map<Integer, RestClient> result = new HashMap<>(nodes.size());
        for (Node node : nodes) {
            RestClient client = RestClient.builder()
                    .baseUrl(node.getHost())
                    .build();
            result.put(node.getId(), client);
        }
        return result;
    }


    public void afterPropertiesSet() {
//        this.localHost = serverAddress + ":" + port;
        List<Node> nodes = nodeRepository.findAll();
        if (nodes.isEmpty()) {
            masterNode = new Node(1, "Master", "http://localhost:" + port, "Master", "", true, 3, 1);
        } else {
            List<Node> master = nodes.stream()
                    .filter(x -> NodeType.Master.name().equals(x.getType()))
                    .toList();
            if (!master.isEmpty()) {
                masterNode = nodes.get(0);
            }else {
                masterNode = nodes.get(0);
            }
        }
        HashMap<Integer, RestClient> nodeMap2 = new HashMap<>(nodes.size());
        for (Node node : nodes) {
            RestClient client = RestClient.builder()
                    .baseUrl(node.getHost())
                    .build();
            nodeMap2.put(node.getId(), client);
        }
        nodeMap = nodeMap2;

    }
// UI


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

    public Node getHost(String tags) {

        if (getNodeList().isEmpty()) { // no correct config
            return masterNode;
        }
        List<Node> workerNodes = new ArrayList<>(getWorkerHosts());
        if (workerNodes.isEmpty()) {
            List<Node> masters = getNodeList()
                    .stream()
                    .filter(x -> NodeType.Master.name().equals(x.getType()))
                    .toList();
            if (!masters.isEmpty()) {
                return masters.get(0);
            }
            return masterNode;
        }
        List<Node> freeTaggedWorker = checkTags(workerNodes, tags);
        if (freeTaggedWorker.isEmpty()) {
            log.warn("No Workers with correct tag");
            return null;
        }
        if (freeTaggedWorker.size() == 1) {
            log.info(" Only one node. No competition.");
            return freeTaggedWorker.get(0);
        }

        if (!freeTaggedWorker.stream().filter(x -> x.getFreeSlots() > 0).toList().isEmpty()) {
            freeTaggedWorker.sort(Comparator.comparing(Node::getFreeSlots).reversed());
            return freeTaggedWorker.get(0);
        } else {
            log.warn("No free workers!!!");
            return freeTaggedWorker.get(ThreadLocalRandom.current().nextInt(freeTaggedWorker.size()));
        }
    }

    private List<String> splitString(String s) {
        if (s == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(s.split(",")).map(String::trim).toList();
    }

    private boolean nodeContainsTag(Node node, List<String> requiredTagsList) {
        if (StringUtils.isEmpty(node.getTags())) {
            return true;
        }
        List<String> tags = splitString(node.getTags());
        for (String tag : requiredTagsList) {
            if (tags.contains(tag)) {
                return true;
            }
        }
        return false;
    }

    private List<Node> checkTags(List<Node> nodes, String requiredTags) {
        if (StringUtils.isEmpty(requiredTags)) {
            return nodes;
        }

        List<String> requiredTagsList = Stream.of(requiredTags.split(",")).map(String::trim).toList();
        List<Node> result = new ArrayList<>();
        for (Node node : nodes) {
            if (nodeContainsTag(node, requiredTagsList)) {
                result.add(node);
            }
        }
        return result;
    }

    public  boolean  sendToQueue(StepInstance si,  ScriptDefinition scriptDefinition) throws JsonProcessingException {
        Optional<Script> opScript = scriptRepository.findByScriptDefinition(
                scriptDefinition.getName(),
                scriptDefinition.getLanguage(),
                scriptDefinition.getType(),
                scriptDefinition.getVersion()
                );
        if (opScript.isEmpty()){
            saveError(si, stepInstanceRepository, null,"Cannot find script :"+scriptDefinition);
            return false;
        }
        Script script = opScript.get();
        log.info("sendToQueue:{}", si);

        ScriptRequest scriptRequest = getScriptRequest(si, script);

        return sendTrToQueue(scriptRequest, si);
    }

    public boolean sendTrToQueue(ScriptRequest scriptRequest, StepInstance si) throws JsonProcessingException {
        //TODO
        Node node = getHost(si.getTags());
        if (node == null) {
            si.addLog("Cannot found queue node!");
            return false;
        }

        RestClient rc = nodeMap.get(node.getId());
        if (rc == null) {
            si.addLog("Cannot found rest client !");
            return false;
        }

        try {
            ResponseEntity<Void> resp= rc.put().uri(enqueuePath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(scriptRequest)
                    .retrieve()
                    .toBodilessEntity();
            if( resp.getStatusCode().is2xxSuccessful()){
                return true;
            }else{
                si.addLog("Error send to Queue:"+resp);
                return false;
            }

        } catch (Throwable e) {
            saveError(si, stepInstanceRepository, e, "cannot send to queue");
            
        }
        return false;
    }


    public PropData sendTestReqst(ScriptRequest testTaskRequest) throws JsonProcessingException {
        //TODO
        Node node = getHost(testTaskRequest.getTags());
        if (node == null) {
            log.error("No host for {}", testTaskRequest.getTags());
            return new PropData(UNKNOWN_ERROR, null,"Node not found!!");
        }

        RestClient rc = nodeMap.get(node.getId());
        if (rc == null) {
            log.info("Cannot found rest client !");
            return new PropData(UNKNOWN_ERROR, null,"Node not found!!");
        }

        try {

            return rc.put().uri("/sync/request")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(testTaskRequest)
                    .retrieve()
                    .body(PropData.class);


        } catch (Throwable e) {
            log.error("Error !!!!",e);
            return new PropData(UNKNOWN_ERROR, null,e.getMessage());
        }

    }
    private static @NotNull ScriptRequest getScriptRequest(StepInstance si, Script script) {
        ScriptRequest taskRequest = new ScriptRequest();
        taskRequest.setRequestId(si.getStepInstanceId());
        ScriptDefinition definition= getScriptDefinition(script);
        taskRequest.setScriptDefinition(definition);
        taskRequest.setStepName(si.getName());
        taskRequest.setVars(si.getVars());
        taskRequest.setLocalResults(si.getLocalResults());
        taskRequest.setEtlResults(si.getEtlInstance().getEtlVars());
        taskRequest.setTags(si.getTags());
        taskRequest.setMaxAttempts(si.getMaxAttempts());


        return taskRequest;
    }

    // --- schedule operation


    public void deleteSchedule(BigInteger etlId){
        ScheduleData data = new ScheduleData(etlId);
        scheduleRestClient
                .put()
                .uri(deleteSchedule)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ScheduleData(etlId))
                .retrieve()
                .toBodilessEntity();

    }

    public void updateSchedule(Etl etl){
        scheduleRestClient
                .put()
                .uri(updateSchedule)
                .contentType(MediaType.APPLICATION_JSON)
                .body(etl)
                .retrieve()
                .toBodilessEntity();

    }

}
