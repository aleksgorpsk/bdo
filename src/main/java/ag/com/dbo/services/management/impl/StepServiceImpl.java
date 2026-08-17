package ag.com.dbo.services.management.impl;

import ag.com.dbo.models.management.StepDTO;
import ag.com.dbo.models.management.Step;
import ag.com.dbo.repositories.management.StepInstanceRepository;
import ag.com.dbo.repositories.management.StepRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

import static ag.com.dbo.services.Utils.setStepSensorFlag;


@Slf4j
@Service
public class StepServiceImpl {

    private final ModelMapper modelMapper;
    private final StepRepository stepRepository;

    public StepServiceImpl(StepInstanceRepository stepInstanceRepository, ModelMapper modelMapper, StepRepository stepRepository) {
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
            stepRepository.save(setStepSensorFlag(mapFrom(stepDTO)));
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
        Step newEtl =stepRepository.saveAndFlush(setStepSensorFlag(mapFrom(stepDTO)));
        return mapFrom(newEtl);
    }

    /*
     * -------------------------------------------------------------------------
     * Retrieve
     * -------------------------------------------------------------------------
     */

    public StepDTO retrieveById(BigInteger id) {
        return stepRepository.findById(id)
                .map(this::mapFrom)
                .orElse(null);
    }

    public void delete(BigInteger id) {
//        stepRepository
         stepRepository.deleteById(id);
    }

    public StepDTO mapFrom(Step step) {
        return modelMapper.map(step, StepDTO.class);
    }

    public Step mapFrom(StepDTO dto) {
        return modelMapper.map(dto, Step.class);
    }


}
