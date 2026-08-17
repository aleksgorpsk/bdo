package ag.com.dbo.service;

import ag.com.dbo.models.management.Node;
import ag.com.dbo.repositories.management.NodeRepository;
import ag.com.dbo.services.management.impl.ExternalServiceImpl;
import ag.com.dbo.services.management.impl.NodeServiceImpl;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


import java.util.Optional;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

//@ExtendWith(MockitoExtension.class) // Initializes Mockito annotations automatically
@SpringBootTest
@ActiveProfiles("test")

public class NodeServiceTest {


    @Test
    public void getNodes() {
        // Arrange
        NodeRepository nodeRepository = Mockito.mock(NodeRepository.class);
        ExternalServiceImpl externalService = Mockito.mock(ExternalServiceImpl.class);
        NodeServiceImpl nodeService = new NodeServiceImpl( nodeRepository,  externalService) ;
        Integer nodeId = 1;
        Node mockNode =  new Node(    1, "master","localhost","Master","big,no",true,3,1);

        when(nodeRepository.findById(1)).thenReturn(Optional.of(mockNode));

        // Act
        Optional<Node> result = nodeService.findById(nodeId);


        assertEquals("master", result.get().getName());
        verify(nodeRepository, times(1)).findById(1); // Verifies the repo was called
    }
    @org.junit.jupiter.api.Test
    void getNode() {
        NodeRepository nodeRepository = Mockito.mock(NodeRepository.class);

        when(nodeRepository.existsById(1666)).thenReturn(true);
        when(nodeRepository.existsById(3)).thenReturn(false);

        NodeServiceImpl nodeService= new NodeServiceImpl(nodeRepository,Mockito.mock(ExternalServiceImpl.class) );
        Integer nodeId = 1666;
        nodeService.delete(nodeId);
        assertTrue(nodeService.delete(nodeId));
        assertFalse(nodeService.delete(3));

    }
}
