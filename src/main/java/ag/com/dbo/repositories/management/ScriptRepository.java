package ag.com.dbo.repositories.management;

import ag.com.dbo.models.script.Script;
import ag.com.dbo.models.script.ScriptDefinition;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;


@Repository

public interface ScriptRepository extends JpaRepository<  @NonNull Script,  @NonNull BigInteger> {

    @Query("SELECT s FROM Script s WHERE s.name = :name and s.language = :language Order by s.version DESC LIMIT 1")
    List<Script> getLastScript(@Param("language") String language,
                              @Param("name") String name);
    Page<Script> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    @Query("SELECT s FROM Script s WHERE s.name=:name and s.language=:language and s.type=:type and s.version = :version")
    Optional<Script>  findByScriptDefinition(
            @Param("name") String name,
            @Param("language") String language,
            @Param("type") String type,
            @Param("version") String version
    );

}
