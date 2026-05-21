package ag.com.dbo.controllers;

import ag.com.dbo.models.management.StepInstance;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

@Data
@AllArgsConstructor
public class FullEtlInstance {
    Map<String, Set<String>> parentToChildrenStep = Collections.emptyMap();;
    Map<String, StepInstance> siBase = Collections.emptyMap();
    List<String> finishSteps = Collections.emptyList();
    List<StepInstance> steps = Collections.emptyList();
}
