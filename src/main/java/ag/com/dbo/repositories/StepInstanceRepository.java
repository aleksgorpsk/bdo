package ag.com.dbo.repositories;

import ag.com.dbo.models.Step;
import ag.com.dbo.models.StepInstance;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository

public interface StepInstanceRepository extends JpaRepository<@NonNull StepInstance, @NonNull BigInteger> {

    @Query("SELECT si FROM StepInstance si WHERE si.etlInstance.id = :etlInstanceId")
    List<StepInstance>  findAllStepInstancesByEtlInstanceId(@Param("etlInstanceId") BigInteger etlInstanceId);

}
