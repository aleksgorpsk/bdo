package ag.com.dbo.repositories;

import ag.com.dbo.models.DataLoading;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Repository

public interface DataLoadingRepository extends JpaRepository< @NonNull DataLoading,  @NonNull BigInteger> {

}
