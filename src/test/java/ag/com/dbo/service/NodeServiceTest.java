package ag.com.dbo.service;

import ag.com.dbo.models.management.Node;
import ag.com.dbo.repositories.management.NodeRepository;
import ag.com.dbo.services.management.ExternalService;
import ag.com.dbo.services.management.NodeService;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.Optional;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class) // Initializes Mockito annotations automatically
public class NodeServiceTest {


    @Test
    public void getNodes() {
        // Arrange
        NodeRepository nodeRepository = Mockito.mock(NodeRepository.class);
        ExternalService externalService = Mockito.mock(ExternalService.class);
        NodeService nodeService = new  NodeService( nodeRepository,  externalService) ;
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

        NodeService nodeService= new NodeService(nodeRepository,Mockito.mock(ExternalService.class) );
        Integer nodeId = 1666;
        nodeService.delete(nodeId);
        assertTrue(nodeService.delete(nodeId));
        assertFalse(nodeService.delete(3));

    }
}
