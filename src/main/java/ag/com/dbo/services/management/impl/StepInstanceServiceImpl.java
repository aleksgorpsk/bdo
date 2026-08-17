package ag.com.dbo.services.management.impl;

import ag.com.dbo.controllers.queue.QueueStatus;
import ag.com.dbo.models.management.*;
import ag.com.dbo.models.queue.QueueStorage;
import ag.com.dbo.repositories.management.EtlInstanceRepository;
import ag.com.dbo.repositories.management.StepInstanceRepository;
import ag.com.dbo.services.Utils;
import ag.com.dbo.services.management.StepInstanceService;
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

@Slf4j
@Service
public class StepInstanceServiceImpl implements StepInstanceService {

    private final StepInstanceRepository stepInstanceRepository;
    private final ModelMapper modelMapper;
    private final EtlInstanceRepository etlInstanceRepository;

    public StepInstanceServiceImpl(StepInstanceRepository stepInstanceRepository, ModelMapper modelMapper, EtlInstanceRepository etlInstanceRepository) {
        this.stepInstanceRepository = stepInstanceRepository;
        this.modelMapper = modelMapper;
        this.etlInstanceRepository = etlInstanceRepository;
    }

    /*
     * -------------------------------------------------------------------------
     * Create
     * -------------------------------------------------------------------------
     */

    public StepInstanceDTO create(StepInstanceDTO stepInstanceDTO) {
        StepInstance etl = setStepInstanceSensorFlag(mapFrom(stepInstanceDTO));
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
     * @param etlInstanceId Etl If
     * @param pageable Page parameters
     * @return Page
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
        return e.map(this::mapFrom);
    }

    public List<StepInstanceDTO> retrieveAll() {
        return stepInstanceRepository.findAll().stream()
                .map(this::mapFrom)
                .peek(x-> log.info("etl:"+ x.toString()))
                .toList();
    }

    public List<StepInstance> retrieveAll0() {
        return stepInstanceRepository.findAll();
    }

    public List<StepInstanceDTO> retrievePage() {

        log.info("retrievePage");
        return stepInstanceRepository.findAll().stream()
                .map(this::mapFrom)
                .peek(x-> log.info("etl:"+ x.toString()))
                .toList();
    }

    //    @CachePut(value = "etlInstances", key = "#etlInstanceDTO.etlInstanceId")
    public boolean update(StepInstanceDTO stepInstanceDTO) {
        if (stepInstanceRepository.existsById(stepInstanceDTO.getStepInstanceId())) {
            stepInstanceRepository.save( setStepInstanceSensorFlag(mapFrom(stepInstanceDTO)));
            return true;
        } else {
            return false;
        }
    }


    //    @CacheEvict(value = "etlInstances", key = "#etlInstanceId")
    public boolean delete(String id) {
        if (stepInstanceRepository.existsById(id)) {
            stepInstanceRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    public Page<@NotNull StepInstanceDTO> convert(Page<@NotNull StepInstance> etlp){
        return etlp.map(new Function<StepInstance, StepInstanceDTO>() {
            @Override
            public StepInstanceDTO apply(StepInstance entity) {
                return mapFrom(entity);
            }
        });

    }

    public List<StepInstance> save(List<StepInstance> list){
        return stepInstanceRepository.saveAllAndFlush(list.stream().toList().stream().map(Utils::setStepInstanceSensorFlag).toList());
    }

    public StepInstanceDTO mapFrom(StepInstance step) {
        return modelMapper.map(step, StepInstanceDTO.class);
    }

    public StepInstance mapFrom(StepInstanceDTO dto) {
        return modelMapper.map(dto, StepInstance.class);
    }


    public StepInstance updateStepInstance(QueueStorage result)  {
        Optional<StepInstance> oSi = stepInstanceRepository.findById(result.getTaskId());
        if (oSi.isPresent()){
            StepInstance si = oSi.get();

            if (Objects.equals(result.getStatus(), QueueStatus.SUCCESS.name())) {
             /*   if (si.isSensor()) {
                    si.setStatus(StepStatus.InWait.name());
                } */
                ;
            }else {
                si.setStatus(StepStatus.Failed.name());
            }
            si.addLog("From queue:"+result.getLogMessage());
            EtlInstance ei = si.getEtlInstance();
            try {
                si.setLocalResults(merge(si.getLocalResults(), result.getLocalResults()));
                ei.setEtlVars(merge(ei.getEtlVars(), si.getLocalResults(), si.getName()));
                ei.addLog(si.getLogMessage());
            }catch (Exception e){
                si.setStatus(StepStatus.Failed.name());
                si.addLog("Error:" + e.getMessage());
            }finally {
                stepInstanceRepository.saveAndFlush(si);
                etlInstanceRepository.saveAndFlush(ei);
            }
            return si;
        }else{
            throw new RuntimeException(result.getTaskId()+" not found !!!");
//            log.error("{} not found !!!", result.getTaskId());
        }
    }



    public List<StepInstance> getActiveSensors(){
//        OffsetDateTime.now()
        log.info("Sensor Instant.now().getEpochSecond():"+Instant.now().getEpochSecond());
        return stepInstanceRepository.findActiveSensors(StepStatus.InWait.name(), Instant.now().getEpochSecond());
    }
}
