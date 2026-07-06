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
    private final RestClient scriptRestClient;
    private final NodeRepository nodeRepository;


    public Map<Integer, RestClient> nodeMap;

    private List<Node> nodeList = null;
    @Value("${server.port}")
    public String port;

    @Value("${queue.enqueue.path}")
    private String enqueuePath;

    @Value("${script.process.path}")
    private String scriptRunPath;

    public Node masterNode; // in case master without Workers

    public ExternalService(StepInstanceRepository stepInstanceRepository,
                           @Qualifier("scriptRestClient") RestClient scriptRestClient,
                           NodeRepository nodeRepository) {
        this.stepInstanceRepository = stepInstanceRepository;

        this.scriptRestClient = scriptRestClient;
        this.nodeRepository = nodeRepository;

      //  masterNode = new Node(1, "Master", "http://localhost:" + port, "Master", "", true, 3, 1);


    }


    public void sendToQueue(TaskRequest taskRequest, StepInstance si) throws JsonProcessingException {
        //TODO
        Node node = getHost(si.getTags());
        if (node == null) {
            si.addLog("Cannot found queue node!");
            return;
        }

        RestClient rc = nodeMap.get(node.getId());
        if (rc == null) {
            si.addLog("Cannot found rest client !");
            return;
        }

        try {
            rc.put().uri(enqueuePath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(taskRequest)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Throwable e) {
            saveError(si, stepInstanceRepository, e, "cannot send to queue");

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
}
