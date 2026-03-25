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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

        return ei;

    }
    private void startAllSteps(EtlInstance etl){
        List<Step> steps= stepRepository.findAllStepsByEtl(etl.getEtl().getId());
        List<StepInstance> sis = new ArrayList<>(steps.size());
        for(Step step: steps){
            StepInstance si = new StepInstance();
            si.setEtl(etl.getEtl());
            si.setStep(step);
            if(step.getParentSteps() == null){
                si.setStatus(1);
            }
            sis.add(si);
        }
        List<StepInstance> sisout = stepInstanceRepository.saveAll(sis);
        for (StepInstance si :sisout){

        }
    }
}
