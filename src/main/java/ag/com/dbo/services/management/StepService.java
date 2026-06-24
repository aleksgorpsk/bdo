package ag.com.dbo.services.management;

import ag.com.dbo.models.management.StepDTO;
import ag.com.dbo.models.management.Step;
import ag.com.dbo.repositories.management.StepInstanceRepository;
import ag.com.dbo.repositories.management.StepRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigInteger;


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

    /*
     * -------------------------------------------------------------------------
     * Retrieve
     * -------------------------------------------------------------------------
     */

    @Cacheable(value = "etls", key = "#id")
    public StepDTO retrieveById(BigInteger id) {
        return stepRepository.findById(id)
                .map(this::mapFrom)
                .orElse(null);
    }

    public StepDTO mapFrom(Step step) {
        return modelMapper.map(step, StepDTO.class);
    }

    public Step mapFrom(StepDTO dto) {
        return modelMapper.map(dto, Step.class);
    }


}
