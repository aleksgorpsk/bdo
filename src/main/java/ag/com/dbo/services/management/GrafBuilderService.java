package ag.com.dbo.services.management;

import ag.com.dbo.controllers.model.FieldPlace;
import ag.com.dbo.controllers.model.StepData;
import ag.com.dbo.models.management.EtlDTO;
import ag.com.dbo.models.management.EtlInstance;
import ag.com.dbo.models.management.Step;
import ag.com.dbo.models.graf.Figure;
import ag.com.dbo.models.graf.Line;
import ag.com.dbo.models.graf.Rectangle;
import ag.com.dbo.models.management.StepInstance;
import ag.com.dbo.repositories.management.EtlInstanceRepository;
import ag.com.dbo.repositories.management.StepInstanceRepository;
import ag.com.dbo.repositories.management.StepRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GrafBuilderService {

    private final int hDistance=40;
    private final int vDistance=40;
    private final int height= 100;
    private final int width= 180;


    private final EtlService etlService;
    private final StepRepository stepRepository;
    private final StepInstanceRepository stepInstanceRepository;
    private final EtlInstanceRepository etlInstanceRepository;

    public GrafBuilderService(EtlService etlService, StepRepository stepRepository, StepInstanceRepository stepInstanceRepository, EtlInstanceRepository etlInstanceRepository) {
        this.etlService = etlService;
        this.stepRepository = stepRepository;
        this.stepInstanceRepository = stepInstanceRepository;
        this.etlInstanceRepository = etlInstanceRepository;
    }


    public List<Figure> getFigures(BigInteger etlId) {
        log.info("getFigures:{}", etlId);

        Optional<EtlDTO> etlDto = etlService.findById(etlId);
        if (etlDto.isEmpty()) {
            log.error("etn {} not found !", etlId);
            return Collections.emptyList();
        } else {
            List<Step> steps = stepRepository.findAllStepsByEtl(etlId);

            Map<BigInteger, Step> stepMap = steps.stream().collect(Collectors.toMap(Step::getStepId, Function.identity()));

            Map<BigInteger, List<BigInteger>> childrenStep = new HashMap<>(); //parentId-> List
            for(Map.Entry<BigInteger, Step> entry: stepMap.entrySet()){
                BigInteger[] parents = entry.getValue().getParentStepIds();
                if (ArrayUtils.isNotEmpty(parents)){
                    for (BigInteger parent: parents){
                        List<BigInteger> children = childrenStep.getOrDefault(parent, new ArrayList<>());
                        children.add(entry.getKey());
                        childrenStep.put(parent, children);
                    }
                }else{
                    if (!childrenStep.containsKey(entry.getKey())) {
                        childrenStep.put(entry.getKey(), new ArrayList<>());
                    }
                }
            }
            log.info("childrenStep:{}", childrenStep);
            // build tree root-> children

            List<List<BigInteger>> stepPlan= getStepLines(childrenStep, steps);
            log.info("stepPlan: {}",stepPlan);

            return getFigure(stepPlan, stepMap);
        }
    }

    private int getMaxHeight(List<List<BigInteger>> stepPlan){
        return stepPlan.stream().map(List::size).max(Comparator.naturalOrder()).orElse(0);
    }

    /**
     *
     * @param stepPlan
     * @param stepMap
     * @return
     */
    private  List<Figure> getFigure(List<List<BigInteger>> stepPlan, Map<BigInteger, Step> stepMap ){

        Map<BigInteger, Figure> figureStore = new HashMap<>();
        int hD = hDistance;
        int vD = vDistance;
        int y = height;
        int w = width;
        List<Figure> result = new ArrayList<>();
        boolean start= true;
        int maxHeight = getMaxHeight(stepPlan);

        for (List<BigInteger> leaf: stepPlan){
            int shift = (maxHeight - leaf.size())*(hDistance+height)/2;

            vD = vDistance+ shift ;
            for (BigInteger etl: leaf){
                Step st= stepMap.get(etl);
                Rectangle r = new Rectangle();
                r.setX(hD);
                r.setY(vD);
                r.setH(y);
                r.setW(w);
                r.setColor("green");
                vD = vD + vDistance + height;
                r.setId(etl);
                r.setName(st.getName());
                result.add(r);
                figureStore.put(etl, r);
            }
            hD = hD + width + hDistance;
        }

        List<Figure> lines= new ArrayList<>();
        // line calqulation
        for (Figure f : result){
            Rectangle r = (Rectangle) f;
            Step  step= stepMap.get(f.getId());
            if (step==null){
                log.error("Step {} not found! ",f.getId());
            }else{
                BigInteger[] parents = step.getParentStepIds();
                if ( ArrayUtils.isNotEmpty(parents)) {
                    for (BigInteger id:parents){
                        Rectangle parent= (Rectangle) figureStore.get(id);
                        Line l= new Line();
                        // child
                        l.setX2(r.getX());
                        l.setY2(r.getY()+(r.getH())/2);
                        // parent
                        l.setX1(parent.getX()+parent.getW());
                        l.setY1(parent.getY()+(parent.getH())/2);
                        l.setStrokeWidth(2);
                        l.setStroke("black");
                        lines.add(l);
                    }
                }
            }
        }
        result.addAll(lines);
    return result;
    }


    private List<BigInteger> getNextLine(List<BigInteger> currentLine, Map<BigInteger, List<BigInteger>>  childrenSteps){
        return currentLine
                .stream()
                .filter(childrenSteps::containsKey)
                .map(childrenSteps::get)
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors
                        .toCollection(ArrayList::new));
    }

    private boolean foundLine(int currentLine, BigInteger element, List<List<BigInteger>> result){
        for(int i = 0; i< result.size(); i++) {
            List<BigInteger> searchLine= result.get(i);
            if (i!= currentLine){
                if (searchLine.contains(element)){
                    return  true;
                }
            }
        }
        return  false;
        }

    private List<List<BigInteger>> getStepLines(Map<BigInteger, List<BigInteger>>  childrenSteps,  List<Step> steps) {
        List<List<BigInteger>> result = new ArrayList<>();
        List<BigInteger> newList = steps.stream().filter(s -> ArrayUtils.isEmpty(s.getParentStepIds()))
                .map(Step::getStepId)
                .collect(Collectors
                        .toCollection(ArrayList::new));//toList();
        while(!newList.isEmpty()){
            result.add(newList);
            newList = getNextLine(newList, childrenSteps);
        }
// toDO remove duplicates!
        for(int i = 0; i< result.size(); i++){
            List<BigInteger> line =  result.get(i);
            int finalI = i;
            line.removeIf(x-> foundLine(finalI,x,result) );
        }

       log.info("getStepLines:{}", result);

        return result;
    }

    public StepData  getStepsField(BigInteger etlId) {
        log.info("getStepsField:{}", etlId);
        StepData stepDate = new StepData();

        Optional<EtlDTO> etlDto = etlService.findById(etlId);
        if (etlDto.isEmpty()) {
            log.error("etn {} not found !", etlId);
            return stepDate;
        } else {
            List<StepInstance> steps = stepInstanceRepository.findByEtl(etlId);
            for (StepInstance step : steps){
                String key = step.getEtlInstance().getEtlInstanceId()+":"+step.getStep().getStepId();
                stepDate.getEtlInstanceIds().add(step.getEtlInstance().getEtlInstanceId());
                stepDate.getStepIds().add(step.getStep().getStepId());
                stepDate.getData().put(key, step);
            }
            stepDate.setSteps(stepDate.getStepIds()
                    .stream()
                    .map(stepRepository::getReferenceById)
                    .collect(Collectors.toMap(Step::getStepId, Function.identity())
                    ));
            stepDate.setEtlInstances(
                    stepDate.getEtlInstanceIds()
                            .stream()
                            .map(x-> etlInstanceRepository.getReferenceById(x))
                            .collect(Collectors.toMap(EtlInstance::getEtlInstanceId, Function.identity())
            ));
            return stepDate;
        }
    }

    public List<Step> getSteps(BigInteger etlId) {
        log.info("getSteps:{}", etlId);
        return stepRepository.findAllStepsByEtl(etlId);
    }


}
