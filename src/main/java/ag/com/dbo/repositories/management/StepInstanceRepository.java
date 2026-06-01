package ag.com.dbo.repositories.management;

import ag.com.dbo.models.management.StepInstance;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository

public interface StepInstanceRepository extends JpaRepository<@NonNull StepInstance, @NonNull String> {
    // get all steps instances in etl instance
    @Query("SELECT si FROM StepInstance si WHERE si.etlInstance.etlInstanceId = :etlInstanceId")
    List<StepInstance>  findAllStepInstancesByEtlInstanceId(@Param("etlInstanceId") BigInteger etlInstanceId);

    @Query("SELECT si FROM StepInstance si WHERE si.etlInstance.etlInstanceId = :etlInstanceId")
    Page< @NotNull StepInstance> findByEtlInstance(@Param("etlInstanceId") BigInteger etlInstanceId, Pageable pageable);

//    @Query("SELECT si FROM StepInstance si WHERE upper(si.name) like upper(CONCAT('%', :keyword, '%')) and si.etlInstance.etlInstanceId = :stepInstanceId " )
    @Query("SELECT si FROM StepInstance si WHERE upper(si.name) like upper(CONCAT('%', :keyword, '%')) and si.etlInstance.etlInstanceId = :stepInstanceId " )
    Page<@NonNull StepInstance> findByNameContainingIgnoreCase( @Param("keyword") String keyword, @Param("stepInstanceId") BigInteger  stepInstanceId, Pageable pageable);

    @Query("SELECT si FROM StepInstance si WHERE si.etl.id = :etlid")
    List<StepInstance> findByEtl(@Param("etlid") BigInteger etlId);

    /*

  @Query("SELECT ei FROM EtlInstance ei WHERE ei.etl.id = :etlid")
  Page<EtlInstance> findByEtl(@Param("etlid") BigInteger etlId, Pageable pageable);

   List<EtlInstance> findByEtlIdAndStatus(BigInteger etlId, String Status);
}
     */
}
