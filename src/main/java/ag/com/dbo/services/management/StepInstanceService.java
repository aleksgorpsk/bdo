package ag.com.dbo.services.management;

import ag.com.dbo.controllers.queue.QueueStatus;
import ag.com.dbo.models.management.EtlInstance;
import ag.com.dbo.models.management.StepInstance;
import ag.com.dbo.models.management.StepInstanceDTO;
import ag.com.dbo.models.management.StepStatus;
import ag.com.dbo.models.queue.QueueStorage;
import ag.com.dbo.repositories.management.EtlInstanceRepository;
import ag.com.dbo.repositories.management.StepInstanceRepository;
import ag.com.dbo.services.Utils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static ag.com.dbo.services.Utils.setStepInstanceSensorFlag;
import static ag.com.dbo.services.queue.utils.VarSupport.merge;

public interface StepInstanceService {

    /*
     * -------------------------------------------------------------------------
     * Create
     * -------------------------------------------------------------------------
     */

    StepInstanceDTO create(StepInstanceDTO stepInstanceDTO) ;

    /*
     * -------------------------------------------------------------------------
     * Retrieve
     * -------------------------------------------------------------------------
     */

    StepInstanceDTO retrieveById(String id) ;


    /**
     * return page by EtlInstance
     * @param etlInstanceId
     * @param pageable
     * @return
     */
    Page<@NonNull StepInstanceDTO> retrievePage(BigInteger etlInstanceId, Pageable pageable);


    /**
     * get Page of StepInstanceDTO with search by name with ignoreCase
     * @param etlInstanceId
     * @param keyword
     * @param pageable
     * @return
     */
    Page<@NonNull StepInstanceDTO> findByEtlContainingIgnoreCase(BigInteger etlInstanceId, String keyword, Pageable pageable);

    /**
     * get StepInstanceDTO by Id
     * @param id
     * @return
     */
    Optional<StepInstanceDTO> findById(String id);

    /**
     * get all StepInstanceDTOs
     * @return
     */
    List<StepInstanceDTO> retrieveAll() ;

    /**
     * get all StepInstances
     * @return
     */
    public List<StepInstance> retrieveAll0() ;

    /**
     * get all StepInstanceDTO
     * @return
     */
    public List<StepInstanceDTO> retrievePage() ;

    /**
     * update stepInstanceDTO
     * @param stepInstanceDTO
     * @return
     */

    public boolean update(StepInstanceDTO stepInstanceDTO) ;

    /**
     * delete  etlInstances by id
     * @param id
     * @return
     */

    public boolean delete(String id) ;

    /**
     * get Page with StepInstanceDTO
     * @param etlp
     * @return
     */
    public Page<@NotNull StepInstanceDTO> convert(Page<@NotNull StepInstance> etlp);



    /**
     * update StepInstances
     * @param list
     * @return
     */

    List<StepInstance> save(List<StepInstance> list);

    /**
     * update nextTime for sensors
     * @param result
     * @return
     */
    public StepInstance updateStepInstance(QueueStorage result)  ;

    /**
     * get active sensors wit status  InWait and time out for sensor
     * @return
     */
    List<StepInstance> getActiveSensors();
}
