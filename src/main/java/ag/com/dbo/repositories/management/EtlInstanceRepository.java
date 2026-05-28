package ag.com.dbo.repositories.management;

import ag.com.dbo.models.management.EtlInstance;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository

public interface EtlInstanceRepository extends JpaRepository< @NonNull EtlInstance,  @NonNull BigInteger> {

    @Query("SELECT ei FROM EtlInstance ei WHERE upper(ei.name) like upper(CONCAT('%', :keyword, '%')) and ei.etl.id= :etlId " )
    Page<EtlInstance> findByNameContainingIgnoreCase(@Param("keyword") String keyword, @Param("etlId") BigInteger etlId ,Pageable pageable);

   // @Query("SELECT e FROM Etl e WHERE e.status = :status")
//    List<EtlInstance> findByStatus(@Param("status") Integer status);

  @Query("SELECT ei FROM EtlInstance ei WHERE ei.etl.id = :etlid")
  Page<EtlInstance> findByEtl(@Param("etlid") BigInteger etlId, Pageable pageable);

   List<EtlInstance> findByEtlIdAndStatus(BigInteger etlId, String Status);
}
