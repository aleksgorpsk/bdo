package ag.com.dbo.repositories;

import ag.com.dbo.models.Step;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository

public interface StepRepository extends JpaRepository<@NonNull Step, @NonNull BigInteger> {

    @Query("SELECT s FROM Step s WHERE s.etl.id = :etlId")
    List<Step> findAllStepsByEtl(@Param("etlId") BigInteger etlId);


}
