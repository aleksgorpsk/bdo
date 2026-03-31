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
import org.thymeleaf.util.SetUtils;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
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
            createEtlInstance(etl);
        }
    }
    private EtlInstance createEtlInstance(Etl etl){
        log.info("get one:"+etl);
        EtlInstance ei = new EtlInstance();
        ei.setEtl(etl);
        etlInstanceRepository.saveAndFlush(ei);
        log.info("save  ei:"+ei);
        etl.setStatus(2);
        etlRepository.saveAndFlush(etl);
        log.info("save  etl status:"+ei);
        createStepInstances(ei);
        return ei;
    }
    private void createStepInstances(EtlInstance etl){
        log.info("steps:"+etl);
        List<Step> steps= stepRepository.findAllStepsByEtl(etl.getEtl().getId());
        log.info("steps1:"+steps);

        List<StepInstance> sis = new ArrayList<>(steps.size());
        for(Step step: steps){
            StepInstance si = new StepInstance();
            si.setEtl(etl.getEtl());
            si.setStep(step);
            si.setEtlInstance(etl);
            log.info("si:"+si);
            sis.add(si);
        }
        log.info("steps sis:"+sis);

        List<StepInstance> sisout = stepInstanceRepository.saveAllAndFlush(sis);
        log.info("saved new etl status:"+sisout);

        Map<BigInteger,BigInteger> stepInstanceRelation= new HashMap<>(sisout.size());
        for (StepInstance si :sisout){
            stepInstanceRelation.put(si.getStep().getStepId(), si.getStepInstanceId());
        }
        log.info("1!!!!:"+stepInstanceRelation);
        for (StepInstance si :sisout){
            log.info("1!!!!si:"+si);

            BigInteger[] parents = si.getStep().getParentStepIds();
            if (parents != null) {
                parents = Arrays.stream(parents).map(stepInstanceRelation::get).toArray(BigInteger[]::new);
                si.setParentStepInstanceIds(parents);
            }
            StepInstance sisoute = stepInstanceRepository.saveAndFlush(si);
            log.info("saved updated etl status:"+sisoute);
        }
        log.info("2:"+stepInstanceRelation);
        makeStep(etl);
    }

    private void makeStep(EtlInstance etlInstance){

        log.info("etlInstance id:"+etlInstance.getEtlInstanceId());

        List<StepInstance> steps= stepInstanceRepository.findAllStepInstancesByEtlInstanceId(etlInstance.getEtlInstanceId());
       Map<BigInteger, StepInstance> siBase =   steps.stream().collect(
               Collectors.toMap(StepInstance::getStepInstanceId, Function.identity())
       );

       Map<BigInteger, Set<BigInteger>> parentToChildrenStep= new HashMap<>();
       for(StepInstance si : steps) {
           if (ArrayUtils.isNotEmpty(si.getParentStepInstanceIds())){
               for (BigInteger parentId : si.getParentStepInstanceIds()) {
                   Set<BigInteger> children = parentToChildrenStep.get(parentId);
                   if (children == null) {
                       children = new HashSet<>(Collections.singletonList(si.getStepInstanceId()));
                       parentToChildrenStep.put(parentId, children);
                   } else {
                       children.add(si.getStepInstanceId());
                   }
               }
           }
       }

       log.info("list tree:"+siBase);

       List<StepInstance> rootSteps = steps.stream().filter(x-> ArrayUtils.isEmpty(x.getParentStepInstanceIds())).toList();
       for (StepInstance si : rootSteps){
           if (si.getStatus() == null){
               startStep(si);
           }else if ( si.getStatus()==1 ){  // in progress do nothing

           } else if (si.getStatus() == 4) { // failed do nothing

           } else if (si.getStatus() == 3) { // success
               runRecursive(parentToChildrenStep, si, siBase);
           }

       }


    }

    private void runRecursive(Map<BigInteger, Set<BigInteger>> parentToChildrenStep, StepInstance root, Map<BigInteger, StepInstance> siBase){
        Set<BigInteger> steps = parentToChildrenStep.get(root.getStepInstanceId());

        if (SetUtils.isEmpty(steps)){ // finish !
            return;
        }
        for (BigInteger siId: steps){
            StepInstance si = siBase.get(siId);
            if (si.getStatus() == null){
                startStep(si);
        }


        }

    }

    private void startStep(StepInstance si){
        log.info("startStep"+si);
        si.setStatus(1);

        stepInstanceRepository.saveAndFlush(si);

    }
}
