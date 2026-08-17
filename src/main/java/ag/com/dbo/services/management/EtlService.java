package ag.com.dbo.services.management;

import ag.com.dbo.models.management.Etl;
import ag.com.dbo.models.management.EtlDto;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface EtlService {
    /**
     * create Etl
     * @param etlDTO original
     * @return copy
     */
    EtlDto create(EtlDto etlDTO) ;

    /**
     * get By id
     * @param id ig for search
     * @return copy
     */
    EtlDto retrieveById(BigInteger id) ;

    /**
     * get page of EtlDto by PageRequest
     * @param pageable page parameters
     * @return page of EtlDto
     */
    Page<@NonNull EtlDto> retrievePage(PageRequest pageable);

    /**
     *
     * @param pageable
     * @return
     */
     Page<@NonNull EtlDto> retrievePage(Pageable pageable);

    /**
     * Page of EtlDto with filter with Ignore case
     * @param keyword
     * @param pageable
     * @return page of EtlDto
     */
     Page<@NonNull EtlDto> findByEtlContainingIgnoreCase(String keyword, Pageable pageable);

    /**
     * get EtlDto by Id
     * @param id
     * @return
     */
    Optional<EtlDto> findById(BigInteger id);

    /**
     * get Etl by Id
     * @param id
     * @return Optional<Etl>
     */
    Optional<Etl> findEtlById(BigInteger id);

    /**
     *  get List of EtlDto with status
     * @param status
     * @return
     */
    List<EtlDto> findByStatus(Integer status);

    /**
     *  get List of all EtlDto
     * @return
     */
    public List<EtlDto> retrieveAll();

    /**
     * get all Etl
     * @return
     */
    public List<Etl> retrieveAll0();

    /**
     * get all EtlDto s
     * @return
     */
    public List<EtlDto> retrievePage() ;

    /**
     * update EtlDto
     * @param etlDTO
     * @return updated or not
     */

    public boolean update(EtlDto etlDTO);


    /**
     * delete Etl by Id
     * @param id
     * @return deleted or not
     */
    public boolean delete(BigInteger id) ;

}
