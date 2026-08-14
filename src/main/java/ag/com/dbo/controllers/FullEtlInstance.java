package ag.com.dbo.controllers;

import ag.com.dbo.models.management.StepInstance;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class FullEtlInstance {
    Map<String, Set<String>> parentToChildrenStep = Collections.emptyMap();;
    Map<String, StepInstance> siBase = Collections.emptyMap();
    List<String> finishSteps = Collections.emptyList();
    List<StepInstance> steps = Collections.emptyList();

    public List<String> getCorrectWayInIds(List<String> correctNames, String stepInstanceId){

        return parentToChildrenStep.get(stepInstanceId).stream()
                .map(id-> siBase.get(id))
                .filter(x-> correctNames.contains(x.getName().trim()))
                .map(StepInstance::getStepInstanceId).toList();
    }

    public List<String> getIncorrectWayIds(List<String> correctNames, String stepInstanceId){
        return parentToChildrenStep.get(stepInstanceId).stream()
                .map(id-> siBase.get(id))
                .filter(x-> !correctNames.contains(x.getName()))
                .map(StepInstance::getStepInstanceId).toList();

    }
// Branch1
// Branch1
}
