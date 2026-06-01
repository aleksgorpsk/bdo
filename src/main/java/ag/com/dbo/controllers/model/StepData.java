package ag.com.dbo.controllers.model;

import ag.com.dbo.models.management.EtlInstance;
import ag.com.dbo.models.management.Step;
import ag.com.dbo.models.management.StepInstance;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.*;

@Slf4j
@Data
public class StepData {

    private Set<BigInteger> etlInstanceIds ;
    private Set<BigInteger> stepIds;

    private Map<String, StepInstance> data;
    private Map<BigInteger, EtlInstance> etlInstances;
    private Map<BigInteger, Step> steps;

    public StepData(){
        etlInstanceIds= new HashSet<>();
        stepIds = new HashSet<>();
        data = new HashMap<>();
        etlInstances = new HashMap<>();
        steps = new HashMap<>();
    }
}
