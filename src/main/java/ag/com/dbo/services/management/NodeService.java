package ag.com.dbo.services.management;

import ag.com.dbo.models.management.*;
import ag.com.dbo.models.management.statuses.QueueInfo;
import ag.com.dbo.repositories.management.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class NodeService implements InitializingBean {

    private final NodeRepository nodeRepository;

    private final ExternalService externalService;
    private List<Node> nodeList = null;
    private String localHost;
    @Value("${server.port}")
    private String port;

    public NodeService(NodeRepository nodeRepository, ExternalService externalService) {
        this.nodeRepository = nodeRepository;
        this.externalService = externalService;

    }

    @Scheduled(cron = "${dbo.queue.collect.schedule}")
    private void updateBusyWorkers() {
        List<Node> worker = getWorkerHosts();
        worker.forEach(x -> {
            QueueInfo info = externalService.getInfo(x.getHost());
            if(info!=null) {
                x.setFreeSlots(info.getFreeSlots());
                x.setBusy(info.getBusy());
            }
        });
    }

    private List<Node> getNodeList() {
        if (nodeList == null) {
            nodeList = nodeRepository.findAll().stream().filter(Node::getActive).toList();
        }
        return nodeList;
    }

    private List<Node> getWorkerHosts() {
        return getNodeList().stream()
                .filter(Node::getActive)
                .filter(x -> NodeType.Worker.name().equals(x.getType()))
                .toList();
    }

    public String getHost() {

        if (getNodeList() == null) {
            return localHost;
        }
        List<Node> freeNodes = new ArrayList<>(getWorkerHosts());
        if (freeNodes.isEmpty()) {
            return this.localHost;
        }
        if (freeNodes.size() == 1) {
            return freeNodes.get(0).getHost();
        }
        if (freeNodes.stream().filter(x -> x.getFreeSlots() > 0).toList().isEmpty()) {
            freeNodes.sort(Comparator.comparing(Node::getFreeSlots).reversed());
            return freeNodes.get(0).getHost();
        } else {
            log.warn("No free workers!!!");
            return freeNodes.get(ThreadLocalRandom.current().nextInt(freeNodes.size())).getHost();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String host = InetAddress.getLocalHost().getHostName();
        this.localHost = host + ":" + port;
    }

}
