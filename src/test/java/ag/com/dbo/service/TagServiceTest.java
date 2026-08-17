package ag.com.dbo.service;


import ag.com.dbo.models.management.Node;
import ag.com.dbo.models.management.NodeType;
import ag.com.dbo.repositories.management.NodeRepository;
import ag.com.dbo.repositories.management.ScriptRepository;
import ag.com.dbo.repositories.management.StepInstanceRepository;
import ag.com.dbo.services.management.impl.ExternalServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "server.port=9081",    "myapp.timeout=5000"})
@ActiveProfiles("test")
@Transactional

public class TagServiceTest {

    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private StepInstanceRepository stepInstanceRepository;

    private ExternalServiceImpl getExternalService(){
        RestClient scriptRestClient = Mockito.mock(RestClient.class);
        ScriptRepository scriptRepository =Mockito.mock(ScriptRepository.class);
        ExternalServiceImpl externalService = new ExternalServiceImpl(this.stepInstanceRepository,
                this.nodeRepository,
                scriptRepository);
        externalService.port="9051";
        return  externalService;
    }

    @Test
    public void testEmptyData() {
     ExternalServiceImpl externalService = getExternalService();
        assertDoesNotThrow(externalService::afterPropertiesSet);

        Node node= externalService.getHost(null);
        assertEquals("http://localhost:"+ externalService.port, node.getHost());

    }

    @Test
    public void testOneMasterData() {

        nodeRepository.save(  new Node( null, "Master", "http://localhost:8888", "Master", "",true,3,1));


        Node node= getExternalService().getHost(null);
        assertEquals("http://localhost:8888", node.getHost());

    }

    @Test
    public void testOneMasterOneWorkerData() {

        nodeRepository.save(  new Node( null, "Master", "http://localhost:8880",  NodeType.Master.name(), "",true,3,1));
        nodeRepository.save(  new Node( null, "Worker1", "http://localhost:8881", NodeType.Worker.name(), "",true,3,1));

        Node node= getExternalService().getHost(null);
        assertEquals("http://localhost:8881", node.getHost());

    }


    @Test
    public void testOneMasterManyWorkerData() {

        nodeRepository.save(  new Node( null, "Master", "http://localhost:8880",  NodeType.Master.name(), "",true,3,1));
        nodeRepository.save(  new Node( null, "Worker1", "http://localhost:8881", NodeType.Worker.name(), "",true,3,1));
        nodeRepository.save(  new Node( null, "Worker2", "http://localhost:8882", NodeType.Worker.name(), "",true,3,1));
        nodeRepository.save(  new Node( null, "Worker2", "http://localhost:8883", NodeType.Worker.name(), "",true,3,1));

        Node node= getExternalService().getHost(null);
        assertTrue(node.getHost().contains("http://localhost:888"));

    }

    @Test
    public void testOneMasterManyWorkerDataNoEmpty() {

        nodeRepository.save(  new Node( null, "Master", "http://localhost:8880",  NodeType.Master.name(), "",true,0,1));
        nodeRepository.save(  new Node( null, "Worker1", "http://localhost:8881", NodeType.Worker.name(), "",true,0,1));
        nodeRepository.save(  new Node( null, "Worker2", "http://localhost:8882", NodeType.Worker.name(), "",true,0,1));
        nodeRepository.save(  new Node( null, "Worker2", "http://localhost:8883", NodeType.Worker.name(), "",true,0,1));

        Node node= getExternalService().getHost(null);
        assertTrue(node.getHost().contains("http://localhost:888"));

    }

    @Test
    public void testOneMasterManyWorkerDataTag() {

        nodeRepository.save(  new Node( null, "Master", "http://localhost:8880",  NodeType.Master.name(), "",true,3,1));
        nodeRepository.save(  new Node( null, "Worker1", "http://localhost:8881", NodeType.Worker.name(), "aa",true,3,1));
        nodeRepository.save(  new Node( null, "Worker2", "http://localhost:8882", NodeType.Worker.name(), "bb",true,3,1));
        nodeRepository.save(  new Node( null, "Worker2", "http://localhost:8883", NodeType.Worker.name(), "cc",true,3,1));

        Node node= getExternalService().getHost("bb");
        assertEquals("http://localhost:8882", node.getHost());

    }

    @Test
    public void testRestClientData() throws Exception {

        nodeRepository.save(  new Node( null, "Master", "http://localhost:8880",  NodeType.Master.name(), "",true,3,1));
        nodeRepository.save(  new Node( null, "Worker1", "http://localhost:8881", NodeType.Worker.name(), "aa",true,3,1));
        nodeRepository.save(  new Node( null, "Worker2", "http://localhost:8882", NodeType.Worker.name(), "bb",true,3,1));
        nodeRepository.save(  new Node( null, "Worker2", "http://localhost:8883", NodeType.Worker.name(), "cc",true,3,1));
        ExternalServiceImpl es= getExternalService();
        assertEquals(es.nodeMap, null);

        assertDoesNotThrow(es::afterPropertiesSet);

        assertEquals(es.nodeMap.size(), 4);

    }
}
