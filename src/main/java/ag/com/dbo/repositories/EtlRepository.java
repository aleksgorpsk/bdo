package ag.com.dbo.repositories;

import ag.com.dbo.models.Etl;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Modifying;

import java.math.BigInteger;
import java.util.List;

@Repository
//public interface EtlRepository extends CrudRepository < Etl, BigInteger> {

public interface EtlRepository extends JpaRepository<  @NonNull Etl,  @NonNull BigInteger> {

    @Query("SELECT e FROM Etl e WHERE e.status = :status")
    List<Etl> findByStatus(@Param("status") Integer status);

    Page<Etl> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    @Query("UPDATE Etl t SET t.active = :published WHERE t.id = :id")
    @Modifying
    public void updatePublishedStatus(Integer id, boolean published);

}
