package ag.com.dbo.repository;


import ag.com.dbo.models.management.Node;
import ag.com.dbo.repositories.management.NodeRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class NodeRepositoryTest {
    @Autowired
    private NodeRepository nodeRepository;

    @Test
    public void testSaveAndFindByEmail() {
        // Arrange
        Node node = new Node(    null, "master","localhost","Master","big,no",true,3,1);

        // Act
        nodeRepository.save(node);

        List<Node> foundUser = nodeRepository.findAll();

        // Assert
        assertNotNull(foundUser);
        assertEquals(1, foundUser.size());

    }
}
