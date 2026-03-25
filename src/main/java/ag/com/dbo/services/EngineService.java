package ag.com.dbo.services;

import ag.com.dbo.models.Etl;
import ag.com.dbo.models.EtlInstance;
import ag.com.dbo.models.Step;
import ag.com.dbo.models.StepInstance;
import ag.com.dbo.repositories.EtlInstanceRepository;
import ag.com.dbo.repositories.EtlRepository;
import ag.com.dbo.repositories.StepInstanceRepository;
import ag.com.dbo.repositories.StepRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EngineService {

    private final EtlRepository etlRepository;
    private final EtlInstanceRepository etlInstanceRepository;
    private final StepRepository stepRepository;
    private final StepInstanceRepository stepInstanceRepository;

    public EngineService(EtlRepository etlRepository, EtlInstanceRepository etlInstanceRepository, StepRepository stepRepository, StepInstanceRepository stepInstanceRepository) {
        this.etlRepository = etlRepository;
        this.etlInstanceRepository = etlInstanceRepository;
        this.stepRepository = stepRepository;
        this.stepInstanceRepository = stepInstanceRepository;
    }

    @Scheduled (fixedRateString = "${scheduler.testInterval}", timeUnit = TimeUnit.SECONDS)
    public void schedule(){
        log.info("sh!");
        List<Etl> started = etlRepository.findByStatus(1);
        log.info("get:"+started);
        for (Etl etl : started){
            startEtlInstance(etl);

        }
    }
    private EtlInstance startEtlInstance(Etl etl){
        log.info("get one:"+etl);
        EtlInstance ei = new EtlInstance();
        ei.setEtl(etl);
        etlInstanceRepository.save(ei);
        log.info("save  ei:"+ei);
        etl.setStatus(2);
        etlRepository.save(etl);
        log.info("save  etl status:"+ei);
        startAllSteps(ei);
        return ei;

    }
    private void startAllSteps(EtlInstance etl){
        log.info("steps:"+etl);
        List<Step> steps= stepRepository.findAllStepsByEtl(etl.getEtl().getId());
        log.info("steps1:"+steps);

        List<StepInstance> sis = new ArrayList<>(steps.size());
        for(Step step: steps){
            StepInstance si = new StepInstance();
            si.setEtl(etl.getEtl());

            si.setStep(step);
            if(ArrayUtils.isEmpty(step.getParentSteps())){
                si.setStatus(1);
            }
            log.info("si:"+si);
            sis.add(si);
        }
        log.info("steps sis:"+sis);

        List<StepInstance> sisout = stepInstanceRepository.saveAll(sis);
        log.info("saved new etl status:"+sisout);

        Map<BigInteger,BigInteger> stepInstanceRelation= new HashMap<>(sisout.size());
        for (StepInstance si :sisout){
            // map stepId-> stepInstanceId
            stepInstanceRelation.put(si.getStep().getStepId(), si.getStepInstanceId());
        }
        for (StepInstance si :sisout){
            si.setParentStepInstanceIds(
                    Arrays.stream( si.getParentStepInstanceIds())
                            .map(stepInstanceRelation::get).toArray(BigInteger[]::new));
        }



    }
}
