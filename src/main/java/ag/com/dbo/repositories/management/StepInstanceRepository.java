package ag.com.dbo.repositories.management;

import ag.com.dbo.models.management.StepInstance;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository

public interface StepInstanceRepository extends JpaRepository<@NonNull StepInstance, @NonNull BigInteger> {
    // get all steps instances in etl instance
    @Query("SELECT si FROM StepInstance si WHERE si.etlInstance.id = :etlInstanceId")
    List<StepInstance>  findAllStepInstancesByEtlInstanceId(@Param("etlInstanceId") BigInteger etlInstanceId);

}
