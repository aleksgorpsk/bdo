package ag.com.dbo.services;

import ag.com.dbo.models.*;
import ag.com.dbo.repositories.EtlInstanceRepository;
import ag.com.dbo.repositories.EtlRepository;
import ag.com.dbo.repositories.StepInstanceRepository;
import ag.com.dbo.repositories.StepRepository;
import ag.com.dbo.services.loadingService.MultithreadExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
    private final MultithreadExecutor multithreadExecutor;


    public EngineService(EtlRepository etlRepository, EtlInstanceRepository etlInstanceRepository,
                          StepRepository stepRepository, StepInstanceRepository stepInstanceRepository,
                          MultithreadExecutor multithreadExecutor) {
        this.etlRepository = etlRepository;
        this.etlInstanceRepository = etlInstanceRepository;
        this.stepRepository = stepRepository;
        this.stepInstanceRepository = stepInstanceRepository;
        this.multithreadExecutor = multithreadExecutor;
    }


//    @Scheduled(fixedRateString = "${scheduler.testInterval}", timeUnit = TimeUnit.SECONDS)
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
//  select all steps from etl
        List<StepInstance> steps= stepInstanceRepository.findAllStepInstancesByEtlInstanceId(etlInstance.getEtlInstanceId());
        // build map id -> stepInstances
        Map<BigInteger, StepInstance> siRootBase =   steps.stream().collect(
                Collectors.toMap(StepInstance::getStepInstanceId, Function.identity())
        );

        // make a map: parens Step instance to list of children
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

        log.info("list tree:"+siRootBase);

        // try to run recursive from root
        List<StepInstance> rootSteps = steps.stream().filter(x-> ArrayUtils.isEmpty(x.getParentStepInstanceIds())).toList();
        for (StepInstance si : rootSteps){
            runRecursive(parentToChildrenStep, si, siRootBase);
            /*
           if (si.getStatus() == null){
               startStep(si);
           }else if ( si.getStatus()== StepStatus.InProgres.getStatus() ){  // in progress do nothing
           } else if (si.getStatus() == StepStatus.Failed.getStatus() ) { // failed do nothing
           } else if (si.getStatus() == StepStatus.Queue.getStatus() ) { // in queue do nothing
           } else if (si.getStatus() == StepStatus.Success.getStatus()) { // success
               runRecursive(parentToChildrenStep, si, siRootBase);
           }
            */
        }

    }

    private void runRecursive(Map<BigInteger, Set<BigInteger>> parentToChildrenStep, StepInstance currentSi, Map<BigInteger, StepInstance> siBase){

        if (currentSi.getStatus()==null) { // not started yet
            startStep(currentSi);
        } else if (currentSi.getStatus() == StepStatus.InProgres.getStatus()) {// in progress do nothing
            return;
        } else if (currentSi.getStatus() == StepStatus.Failed.getStatus()) {
            return;
        } else if (currentSi.getStatus() == StepStatus.Queue.getStatus()) { // in queue do nothing
            return;
        } else if (currentSi.getStatus() == StepStatus.Success.getStatus()) { // success
            List<StepInstance> chils = parentToChildrenStep.get(currentSi).stream()
                    .map(siBase::get)
                    .filter(Objects::nonNull)
                    .toList();

            log.info ("Child list: {}",chils);
        }
    }



    private void startStep(StepInstance si){
        log.info("startStep"+si);
        if (si.getStep().getStepActive()) {
            si.setStatus(5);
            stepInstanceRepository.saveAndFlush(si);
            multithreadExecutor.exec(si);
        }else{
            log.info("startStep:{} inactive",si);
        }

    }

}
