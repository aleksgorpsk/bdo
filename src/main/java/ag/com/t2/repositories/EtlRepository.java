package ag.com.t2.repositories;

import ag.com.t2.models.Etl;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository
public interface EtlRepository extends CrudRepository < Etl, BigInteger> {

}
