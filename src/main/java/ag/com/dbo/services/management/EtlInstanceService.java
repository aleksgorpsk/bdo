package ag.com.dbo.services.management;

import ag.com.dbo.models.management.EtlInstance;
import ag.com.dbo.models.management.EtlInstanceDto;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

/**
 *
 */

public interface EtlInstanceService {

    /**
     *  Get EtlInstanceDto by Id
     * @param id BigInteger EtlInstance Id
     * @return
     */

    EtlInstanceDto retrieveById(BigInteger id) ;

    /**
     *  Get page of EtlInstanceDto by id and PageRequest
     * @param etlId EtlInstanceDto id
     * @param pageable pate parameters
     * @return Page< EtlInstanceDto>
     */
    Page<@NonNull EtlInstanceDto> retrievePage(BigInteger etlId, PageRequest pageable);

    /**
     *  Get page of EtlInstanceDto by id and Page
     * @param etlId
     * @param pageable
     * @return
     */
    Page<@NonNull EtlInstanceDto> retrievePage(BigInteger etlId, Pageable pageable );

    /**
     * get page of EtlInstanceDto for search (Ignore case)
     * @param etlId
     * @param keyword
     * @param pageable
     * @return
     */
    Page<@NonNull EtlInstanceDto> findByEtlContainingIgnoreCase(BigInteger etlId, String keyword, Pageable pageable);

    /**
     * get EtlInstanceDto by id
     * @param id
     * @return
     */
    Optional<EtlInstanceDto> findById(BigInteger id);

    /**
     * Get list of EtlInstanceDto by etlId and status
     * @param etlId
     * @param status
     * @return
     */
    List<EtlInstanceDto> findByStatus(BigInteger etlId, String status);

    /**
     * get all EtlInstanceDto
     * @return
     */
    List<EtlInstanceDto> retrieveAll() ;

    /**
     * get all EtlInstance
     * @return
     */

    List<EtlInstance> retrieveAll0();

    /**
     * get all EtlInstanceDto
     * @return
     */
    List<EtlInstanceDto> retrievePage() ;


    /**
     * update EtlInstanceDto
     * @param etlDTO
     * @return
     */


    boolean update(EtlInstanceDto etlDTO) ;

    /**
     * delete EtlInstance by Id
     * @param id
     * @return
     */

    boolean delete(BigInteger id) ;

}
