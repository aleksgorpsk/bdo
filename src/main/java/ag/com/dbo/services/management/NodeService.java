package ag.com.dbo.services.management;

import ag.com.dbo.models.management.Node;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface NodeService {


    /**
     * get Pageable Nodes
     * @param pageable
     * @return Page
     */
    Page<@NonNull Node> retrievePage(Pageable pageable);

    /**
     * get Page with Nodes for search
     * @param keyword for search
     * @param pageable
     * @return
     */
    Page<@NonNull Node> findByEtlContainingIgnoreCase(String keyword, Pageable pageable);

    /**
     * get Node by id
     * @param id
     * @return Optional<Node>
     */
    public Optional<Node> findById(Integer id);

    /**
     * delete node by id
     * @param id
     * @return
     */
    public boolean delete(Integer id) ;

    /**
     * create new Node in db
     * @param node original
     * @return new Node
     */
    public Node create(Node node) ;

    /**
     * Update Node
     * @param node mew node
     * @return copy
     */
    public boolean update(Node node) ;
}
