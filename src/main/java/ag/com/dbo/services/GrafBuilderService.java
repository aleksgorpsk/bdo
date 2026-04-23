package ag.com.dbo.services;

import ag.com.dbo.models.EtlDTO;
import ag.com.dbo.models.Step;
import ag.com.dbo.models.graf.Figure;
import ag.com.dbo.models.graf.Line;
import ag.com.dbo.models.graf.Rectangle;
import ag.com.dbo.repositories.StepRepository;
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

    public GrafBuilderService(EtlService etlService, StepRepository stepRepository) {
        this.etlService = etlService;
        this.stepRepository = stepRepository;
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
                    childrenStep.put(entry.getKey(), new ArrayList<>());
                }
            }
            log.info("childrenStep:{}", childrenStep);
            // build tree root-> children

            List<Step> rootSteps = steps.stream().filter(s -> ArrayUtils.isEmpty(s.getParentStepIds())).toList();
            log.info("rootSteps:{}", rootSteps.stream().map(Step::getStepId).toList());
            List<List<BigInteger>> stepPlan= getStepPlan(childrenStep, rootSteps);
            log.info("stepPlan: {}",stepPlan);

            return getFigure(stepPlan,stepMap);
        }
    }


    private int getMaxHeight(List<List<BigInteger>> stepPlan){
        return stepPlan.stream().map(List::size).max(Comparator.naturalOrder()).orElse(0);
    }
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
                Rectangle r = new Rectangle();
                r.setX(hD);
                r.setY(vD);
                r.setH(y);
                r.setW(w);
                r.setColor("green");
                vD = vD + vDistance + height;
                r.setId(etl);
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


    private List<List<BigInteger>> getStepPlan(Map<BigInteger, List<BigInteger>>  childrenStep, List<Step>  rootSteps) {
        List<List<BigInteger>> plan = new ArrayList<>();
        List<BigInteger> leaf = rootSteps.stream().map(Step::getStepId).toList();
        while (!leaf.isEmpty()) {
            plan.add(leaf);
            List<BigInteger> leaf2 = new ArrayList<>();

            for (BigInteger stepId : leaf) {
                List<BigInteger> leaf3 = childrenStep.getOrDefault(stepId, Collections.emptyList());
                if (!leaf3.isEmpty()) {
                    leaf2.addAll(leaf3);
                }
            }
            leaf = leaf2;
        }
        return plan;
    }


}
