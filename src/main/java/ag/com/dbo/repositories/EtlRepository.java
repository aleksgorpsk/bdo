package ag.com.dbo.repositories;

import ag.com.dbo.models.Etl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
//public interface EtlRepository extends CrudRepository < Etl, BigInteger> {

public interface EtlRepository extends JpaRepository< Etl, BigInteger> {
}
