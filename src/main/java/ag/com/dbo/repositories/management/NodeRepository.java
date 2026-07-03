package ag.com.dbo.repositories.management;

import ag.com.dbo.models.management.Node;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository

public interface NodeRepository extends JpaRepository< @NonNull Node,  @NonNull Integer> {

    Page<@NotNull Node> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    @Query("SELECT n FROM Node n WHERE n.active = true and n.type = :nodeType ORDER BY n.id DESC")
    List<Node> findActiveNodeByType(String nodeType);

}
