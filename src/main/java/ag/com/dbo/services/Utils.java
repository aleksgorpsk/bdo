package ag.com.dbo.services;

import ag.com.dbo.models.management.StepInstance;
import ag.com.dbo.models.management.StepType;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

public class Utils {
    public static boolean isContainsStepType(StepInstance si, StepType type){
        if (StringUtils.isEmpty(si.getStepType())){
            return false;
        }
        return si.getStepType().contains(type.name());
    }

    public static List<String> getCorrectBranches(StepInstance si){
        if (StringUtils.isEmpty(si.getVars())){
            return Collections.emptyList();
        }

        ReadContext ctx = JsonPath.parse(si.getVars());
        List<String> branches= ctx.read("$."+si.getName()+".branches.*");
        return branches;
    }
}
