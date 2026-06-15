package ag.com.dbo.repositories.management;

import ag.com.dbo.models.script.Script;
import ag.com.dbo.models.script.ScriptId;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository

public interface ScriptRepository extends JpaRepository<  @NonNull Script,  @NonNull ScriptId> {

    @Query("SELECT s FROM Script s WHERE s.scriptId.name = :name and s.scriptId.language = :language Order by s.scriptId.version DESC LIMIT 1")
    List<Script> getLastScript(@Param("language") String language,
                              @Param("name") String name);


}
