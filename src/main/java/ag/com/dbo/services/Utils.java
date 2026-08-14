package ag.com.dbo.services;

import ag.com.dbo.models.checker.ModelName;
import ag.com.dbo.models.checker.ModelParser;
import ag.com.dbo.models.checker.varmodel.CommonModel;
import ag.com.dbo.models.management.Step;
import ag.com.dbo.models.management.StepInstance;
import ag.com.dbo.models.script.Script;
import ag.com.dbo.models.script.ScriptDefinition;
import ag.com.dbo.utils.Constants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static ag.com.dbo.services.queue.utils.VarSupport.*;
import static ag.com.dbo.utils.Utils.getExtendedObjectMapper;
import static ag.com.dbo.utils.Utils.getObjectMapper;

@Slf4j
public class Utils {

    public static List<String> getCorrectBranches(StepInstance si) {
        if (StringUtils.isEmpty(si.getVars())) {
            return Collections.emptyList();
        }

        ReadContext ctx = JsonPath.parse(si.getVars());
        return ctx.read("$." + si.getName() + ".branches.*");
    }

    /*

     ModelParser modelFactory = new ModelParser(varsContent);
        CommonModel cm = modelFactory.getModel(ModelName.branchScript);
     */

    public static CommonModel getSensor(String vars) throws JsonProcessingException {
        ModelParser modelFactory = new ModelParser(vars);
        return modelFactory.getModel(ModelName.sensorScript);
    }
/*
    public static CommonModel getCommonModel(String vars) throws JsonProcessingException {

        return getExtendedObjectMapper().readValue(vars, CommonModel.class);
    }
*/

    public static List<String> getBranches(StepInstance si) throws JsonProcessingException {
        Map<String, Object> result = stringToJsonVar(si.getLocalResults());
        if (result == null) {
            return Collections.EMPTY_LIST;
        }
        Object obranch = result.get(Constants.BRANCH_RESULT_NAME);
        if (obranch instanceof List) {
            return (List) obranch;
        } else if (obranch instanceof String) { // in case one variant
            return List.of((String) obranch);
        } else {

            si.addLog("No data for " + Constants.BRANCH_RESULT_NAME + " in localResult: " + si.getLocalResults());
            return null;
        }
    }


    public static ScriptDefinition getScriptDefinition(Script script) {
        ScriptDefinition result = new ScriptDefinition();
        result.setLanguage(script.getLanguage());
        result.setVersion(script.getVersion());
        result.setName(script.getName());
        return result;
    }

    public static ScriptDefinition getScriptDefinitionByName(String fullScriptName) {
        ScriptDefinition result = new ScriptDefinition();
        String[] ar = fullScriptName.split(":");
        result.setLanguage(ar[0]);
        String name = ar[1];
        result.setName(name);
        if (ar.length > 2) {
            result.setVersion(ar[2]);
        }
        return result;
    }

    public static ScriptDefinition getScriptDefinitionFromFullName(String fullScriptName) {
        ScriptDefinition result = new ScriptDefinition();
        String[] ar = fullScriptName.split(":");
        result.setLanguage(ar[0]);
        String name = ar[1];
        result.setName(name);
        if (name.contains(".")) {
            String[] nameWithExtension = name.split("\\.");
            result.setName(nameWithExtension[0]);
        } else { //TODO legacy, remove !
            result.setName(name);
        }
        if (ar.length > 2) {
            result.setVersion(ar[2]);
        }
        return result;
    }

    public static void addVarToStep(StepInstance step, String varName, Long value) throws JsonProcessingException {
        ObjectMapper objectMapper = getObjectMapper();
        ObjectNode newVar = objectMapper.createObjectNode();
        newVar.put(varName, value);
        step.setVars(merge(step.getVars(), objectMapper.writeValueAsString(newVar)));
    }

    public static StepInstance setStepInstanceSensorFlag(StepInstance si) {
        try {
            ModelParser mp = new ModelParser(si.getVars());
            si.setIsSensor(mp.isSensor());
        } catch (Exception e) {
            log.error("Error in vars:" + e.getMessage());
        }
        return si;
    }

    public static Step setStepSensorFlag(Step stp) {
        try {
            if (StringUtils.isNotEmpty(stp.getVars())) {
                ModelParser mp = new ModelParser(stp.getVars());
                stp.setIsSensor(mp.isSensor());
            }
        } catch (Exception e) {
            log.error("Error in vars:" + e.getMessage());
        }
        return stp;
    }
    public static boolean getStepSensorFlag(String vars) {
        try {
            if (StringUtils.isNotEmpty(vars)) {
                ModelParser mp = new ModelParser(vars);
                return mp.isSensor();
            }
        } catch (Exception e) {
            log.error("Error in vars:" + e.getMessage());
        }
        return false;
    }


}
