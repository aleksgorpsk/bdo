package ag.com.dbo.services.management;

import ag.com.dbo.controllers.queue.QueueStatus;
import ag.com.dbo.models.management.*;
import ag.com.dbo.models.queue.QueueStorage;
import ag.com.dbo.repositories.management.EtlInstanceRepository;
import ag.com.dbo.repositories.management.StepInstanceRepository;
import ag.com.dbo.repositories.management.StepRepository;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import static ag.com.dbo.services.queue.utils.VarSupport.merge;

@Slf4j
@Service
public class StepService {

    private final ModelMapper modelMapper;
    private final StepRepository stepRepository;

    public StepService(StepInstanceRepository stepInstanceRepository, ModelMapper modelMapper, StepRepository stepRepository) {
        this.modelMapper = modelMapper;
        this.stepRepository = stepRepository;
    }
    /*
     * -------------------------------------------------------------------------
     * Update
     * -------------------------------------------------------------------------
     */

    public boolean update(StepDTO stepDTO) {
        if (stepRepository.existsById(stepDTO.getStepId())) {
            stepRepository.save(mapFrom(stepDTO));
            return true;
        } else {
            return false;
        }
    }

    /*
     * -------------------------------------------------------------------------
     * Create
     * -------------------------------------------------------------------------
     */

    public StepDTO create(StepDTO stepDTO) {
        Step etl = mapFrom(stepDTO);
        Step newEtl =stepRepository.saveAndFlush(etl);
        return mapFrom(newEtl);
    }

    public StepDTO mapFrom(Step step) {
        return modelMapper.map(step, StepDTO.class);
    }

    public Step mapFrom(StepDTO dto) {
        return modelMapper.map(dto, Step.class);
    }


}
