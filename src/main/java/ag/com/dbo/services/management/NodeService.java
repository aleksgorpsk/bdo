package ag.com.dbo.services.management;

import ag.com.dbo.models.management.*;
import ag.com.dbo.models.management.statuses.QueueInfo;
import ag.com.dbo.repositories.management.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class NodeService {

    private final NodeRepository nodeRepository;

    private final ExternalService externalService;

    public NodeService(NodeRepository nodeRepository, ExternalService externalService) {
        this.nodeRepository = nodeRepository;
        this.externalService = externalService;

    }

    @Scheduled(cron = "${dbo.queue.collect.schedule}")
    private void updateBusyWorkers() {
        List<Node> worker = externalService.getWorkerHosts();
        worker.forEach(x -> {
            QueueInfo info = externalService.getInfo(x);
            if(info!=null) {
                x.setFreeSlots(info.getFreeSlots());
                x.setBusy(info.getBusy());
            }
        });
    }

    public Page<@NonNull Node> retrievePage(Pageable pageable){
        return  nodeRepository.findAll(pageable);
    }
    public Page<@NonNull Node> findByEtlContainingIgnoreCase(String keyword, Pageable pageable){
        return nodeRepository.findByNameContainingIgnoreCase( keyword,  pageable);
    }

    public Optional<Node> findById(Integer id){
         return nodeRepository.findById(id);
    }

    public boolean delete(Integer id) {
        if (nodeRepository.existsById(id)) {
            nodeRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }
    public Node create(Node node) {
        return  nodeRepository.saveAndFlush(node);

    }
    public boolean update(Node node) {
        if (nodeRepository.existsById(node.getId())) {
            nodeRepository.save(node);
            return true;
        } else {
            return false;
        }
    }
}
