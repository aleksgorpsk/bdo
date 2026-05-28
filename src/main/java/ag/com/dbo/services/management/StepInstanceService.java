package ag.com.dbo.services.management;

import ag.com.dbo.controllers.queue.QueueStatus;
import ag.com.dbo.models.management.QueueResult;
import ag.com.dbo.models.management.StepInstance;
import ag.com.dbo.models.management.StepInstanceDTO;
import ag.com.dbo.models.management.StepStatus;
import ag.com.dbo.repositories.management.StepInstanceRepository;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Slf4j
@Service
public class StepInstanceService {

    private final StepInstanceRepository stepInstanceRepository;
    private final ModelMapper modelMapper;

    public StepInstanceService(StepInstanceRepository stepInstanceRepository, ModelMapper modelMapper) {
        this.stepInstanceRepository = stepInstanceRepository;
        this.modelMapper = modelMapper;
    }

    /*
     * -------------------------------------------------------------------------
     * Create
     * -------------------------------------------------------------------------
     */

    public StepInstanceDTO create(StepInstanceDTO stepInstanceDTO) {
        StepInstance etl = mapFrom(stepInstanceDTO);
        StepInstance newEtl =stepInstanceRepository.saveAndFlush(etl);
        return mapFrom(newEtl);
    }

    /*
     * -------------------------------------------------------------------------
     * Retrieve
     * -------------------------------------------------------------------------
     */

    public StepInstanceDTO retrieveById(String id) {
        return stepInstanceRepository.findById(id)
                .map(this::mapFrom)
                .orElse(null);
    }


    /**
     * return page by EtlInstance
     * @param etlInstanceId
     * @param pageable
     * @return
     */
    public Page<@NonNull StepInstanceDTO> retrievePage(BigInteger etlInstanceId, Pageable pageable){
        Page<@NonNull StepInstance> entities = stepInstanceRepository.findByEtlInstance(etlInstanceId, pageable);
        return entities.map(e-> modelMapper.map(e, StepInstanceDTO.class));

    }


    public Page<@NonNull StepInstanceDTO> findByEtlContainingIgnoreCase(BigInteger etlInstanceId, String keyword, Pageable pageable){
        Page<@NonNull StepInstance> stepPage = stepInstanceRepository.findByNameContainingIgnoreCase( keyword, etlInstanceId, pageable);
        return convert(stepPage);

    }
    public Optional<StepInstanceDTO> findById(String id){
        Optional<StepInstance> e=stepInstanceRepository.findById(id);
        if (e.isPresent()) {
            return Optional.of(mapFrom(e.get()));
        }
        return Optional.empty();
    }
/*
    public List<StepInstanceDTO> findByStatus(BigInteger etlInstanceId, String status){
        return stepInstanceRepository.findByEtlInstanceIdAndStatus(etlInstanceId, status).stream()
                .map(this::mapFrom)
                .peek(x-> log.info("etl by status:"+ x.toString()))
                .toList();

    }
*/
    //    @Cacheable(value = "etlInstances")
    public List<StepInstanceDTO> retrieveAll() {
        return stepInstanceRepository.findAll().stream()
                .map(this::mapFrom)
                .peek(x-> log.info("etl:"+ x.toString()))
                .toList();
    }

    public List<StepInstance> retrieveAll0() {
        return stepInstanceRepository.findAll();
    }

    //   @Cacheable(value = "etlInstances")
    public List<StepInstanceDTO> retrievePage() {

        log.info("retrievePage");
        return StreamSupport.stream(stepInstanceRepository.findAll().spliterator(), false)
                .map(this::mapFrom)
                .peek(x-> log.info("etl:"+ x.toString()))
                .toList();
    }



    /*
     * -------------------------------------------------------------------------
     * Update
     * -------------------------------------------------------------------------
     */

    //    @CachePut(value = "etlInstances", key = "#etlInstanceDTO.etlInstanceId")
    public boolean update(StepInstanceDTO stepInstanceDTO) {
        if (stepInstanceRepository.existsById(stepInstanceDTO.getStepInstanceId())) {
            stepInstanceRepository.save(mapFrom(stepInstanceDTO));
            return true;
        } else {
            return false;
        }
    }

    /*
     * -------------------------------------------------------------------------
     * Delete
     * -------------------------------------------------------------------------
     */

    //    @CacheEvict(value = "etlInstances", key = "#etlInstanceId")
    public boolean delete(String id) {
        if (stepInstanceRepository.existsById(id)) {
            stepInstanceRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    public Page<StepInstanceDTO> convert( Page<StepInstance> etlp){
        Page<StepInstanceDTO> dtoPage = etlp.map(new Function<StepInstance, StepInstanceDTO>() {
            @Override
            public StepInstanceDTO apply(StepInstance entity) {
                return mapFrom(entity);
            }
        });
        return dtoPage;

    }


    public StepInstanceDTO mapFrom(StepInstance step) {
        return modelMapper.map(step, StepInstanceDTO.class);
    }

    public StepInstance mapFrom(StepInstanceDTO dto) {
        return modelMapper.map(dto, StepInstance.class);
    }
    public void updateStepInstance(QueueResult result){
        Optional<StepInstance> oSi = stepInstanceRepository.findById(result.getTaskId());
        if (oSi.isPresent()){
            StepInstance si = oSi.get();

            if (Objects.equals(result.getStatus(), QueueStatus.SUCCESS.name())) {
                si.setStatus(StepStatus.Success.name());
            }else {
                si.setStatus(StepStatus.Failed.name());
            }
            si.addLog("From queue:"+result.getLog());
            si.setStart(result.getStart());
            si.setStop(result.getStop());
            si.setAttempts(result.getAttempt());
            stepInstanceRepository.save(si);
        }else{
            log.error("{} not found !!!", result.getTaskId());
        }

    }
}
