package ag.com.dbo.services;

import ag.com.dbo.models.management.*;
import ag.com.dbo.repositories.management.EtlInstanceRepository;
import ag.com.dbo.repositories.management.EtlRepository;
import ag.com.dbo.repositories.management.StepInstanceRepository;
import ag.com.dbo.repositories.management.StepRepository;
//import ag.com.dbo.services.loadingService.MultithreadExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EngineService {
    private final EtlRepository etlRepository;
    private final EtlInstanceRepository etlInstanceRepository;
    private final StepRepository stepRepository;
    private final StepInstanceRepository stepInstanceRepository;
//    private final MultithreadExecutor multithreadExecutor;


    public EngineService(EtlRepository etlRepository, EtlInstanceRepository etlInstanceRepository,
                          StepRepository stepRepository, StepInstanceRepository stepInstanceRepository
//            , MultithreadExecutor multithreadExecutor
    ) {
        this.etlRepository = etlRepository;
        this.etlInstanceRepository = etlInstanceRepository;
        this.stepRepository = stepRepository;
        this.stepInstanceRepository = stepInstanceRepository;
  //      this.multithreadExecutor = multithreadExecutor;
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

    public EtlInstance createEtlInstance(Etl etl){
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
        for (StepInstance si :sisout){

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

        log.info("make step etlInstance id:"+etlInstance.getEtlInstanceId());
        //get all step instances from etl instances
        List<StepInstance> steps= stepInstanceRepository.findAllStepInstancesByEtlInstanceId(etlInstance.getEtlInstanceId());

        // map Si.id-> Si for one etl instance
        Map<BigInteger, StepInstance> stepInstancesEltInstance =   steps.stream().collect(
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

        log.info("list tree:{}", stepInstancesEltInstance);
        List<BigInteger> finishSteps = stepInstancesEltInstance.keySet().stream().filter( k-> !parentToChildrenStep.containsKey(k)).toList();
        log.info("finishSteps: {}", finishSteps);
        // try to run recursive from root
        List<StepInstance> startSteps = steps.stream().filter(x-> ArrayUtils.isEmpty(x.getParentStepInstanceIds())).toList();
        for (StepInstance si : startSteps){
            runRecursive(parentToChildrenStep, si, stepInstancesEltInstance, finishSteps);
        }

    }

    private void runRecursive(Map<BigInteger, Set<BigInteger>> parentToChildrenStep, StepInstance currentSi, Map<BigInteger, StepInstance> siBase,  List<BigInteger> finishSteps){

        log.info("Step");
        if (currentSi.getStatus()==null) { // not started yet
            startStepInstance(currentSi);

            List<StepInstance> candidateStartChildren = getChildren(currentSi,parentToChildrenStep,siBase).stream().filter( s->s.getStatus()==null).toList();
//            List<StepInstance> candidateStartChildren = getChildren(currentSi,parentToChildrenStep,siBase).stream().toList().stream().filter( s->s.getStatus()==null).toList();
            for(StepInstance si : candidateStartChildren) {
                runRecursive(parentToChildrenStep, si, siBase, finishSteps);
            }
        } else if (currentSi.getStatus() == StepStatus.InProgres.getStatus()) {// in progress do nothing
            return;
        } else if (currentSi.getStatus() == StepStatus.Failed.getStatus()) {
            return;
        } else if (currentSi.getStatus() == StepStatus.Queue.getStatus()) { // in queue do nothing
            return;
        } else if (currentSi.getStatus() == StepStatus.Success.getStatus()) { // success
            return;
        }
    }

    /**
     *  get StepInstances children of StepInstance
     * @param currentSi
     * @param parentToChildrenStep
     * @param siBase
     * @return
     */
  private List<StepInstance>  getChildren(StepInstance currentSi,
                                          Map<BigInteger, Set<BigInteger>> parentToChildrenStep,
                                          Map<BigInteger, StepInstance> siBase){

      return parentToChildrenStep
              .getOrDefault(currentSi.getStepInstanceId(), Collections.<BigInteger>emptySet())
              .stream()
              .map(siBase::get)
              .filter(Objects::nonNull)
              .toList();
  }

    /**
     * Strart StepInstance and return
     * @param si
     * @return
     */
    private boolean startStepInstance(StepInstance si){
        log.info("-------!!!!!!startStep: {}",si);
        if (si.getStep().getStepActive()) {
            try {
                si.setStatus(5);
                stepInstanceRepository.saveAndFlush(si);
/*                PropData data = multithreadExecutor.exec(si);
                si.setResultStatus(data.getResultStatus());
                stepInstanceRepository.saveAndFlush(si);
                si.getEtl().setLastResult(data.getResultStatus());
                etlRepository.saveAndFlush(si.getEtl());
                log.info("R-------!!!!!!startStep : {} return {}", si, data.getResultStatus());
 */
                return true; //data.getResultStatus() == 0;
            }catch (Throwable e){
                si.setResultStatus(-99);
                si.setResultMessage(e.getMessage());
                return true;
            }
        }else{
            log.info("startStep:{} inactive",si);
            si.setResultStatus(-101);  // just inactive
            return true;
        }

    }

}
