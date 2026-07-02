package ag.com.dbo.repositories.management;

import ag.com.dbo.models.management.Node;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository

public interface NodeRepository extends JpaRepository< @NonNull Node,  @NonNull Integer> {

}
