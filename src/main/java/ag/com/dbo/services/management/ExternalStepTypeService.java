package ag.com.dbo.services.management;

import ag.com.dbo.models.checker.BranchModel;
import ag.com.dbo.models.checker.SensorModel;
import ag.com.dbo.models.checker.StepModel;
import ag.com.dbo.models.management.StepInstance;
import ag.com.dbo.models.queue.QueueStorage;
import ag.com.dbo.repositories.management.StepInstanceRepository;
import ag.com.dbo.repositories.queue.QueueStorageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ag.com.dbo.services.queue.utils.VarSupport.stringToJsonVar;
import static ag.com.dbo.utils.Utils.*;

@Slf4j
@Service
public class ExternalStepTypeService {

    private final StepInstanceRepository stepInstanceRepository;
    private final QueueStorageRepository queueStorageRepository;

    public ExternalStepTypeService(StepInstanceRepository stepInstanceRepository, QueueStorageRepository queueStorageRepository) {
        this.stepInstanceRepository = stepInstanceRepository;
        this.queueStorageRepository = queueStorageRepository;
    }

    public SensorModel checkSensor(StepInstance si) throws JsonProcessingException {

        SensorModel model = getSensor(si.getVars());
        if (model.getAttemptTimeOut() == null){
            saveError( si, stepInstanceRepository, null,  "Empty field AttemptTimeOut.");
        }
        if (model.getFailTimeout() == null){
            saveError( si, stepInstanceRepository, null,  "Empty field FailTimeout.");
        }
        if (model.getCheckerScript() == null){
            saveError( si, stepInstanceRepository, null,  "Empty field CheckerScript.");
        }
        if (model.getSensorResultName() == null){
            saveError( si, stepInstanceRepository, null,  "Empty field SensorResultName.");
        }
        return model;
    }

    public SensorModel getSensor(String vars) throws JsonProcessingException {
        return getExtendedObjectMapper().readValue(vars, new TypeReference<SensorModel>(){});

    }

    public BranchModel checkBranch(StepInstance si) throws JsonProcessingException {
        BranchModel model = getBranch(si.getVars());
        if (model.getResultName() == null){
            saveError( si, stepInstanceRepository, null,  "Empty field ResultName.");
        }
        if (model.getBranchScript() == null){
            saveError( si, stepInstanceRepository, null,  "Empty field BranchScript.");
        }
        return model;
    }

    public BranchModel getBranch(String vars) throws JsonProcessingException {
        return getExtendedObjectMapper().readValue(vars, new TypeReference<BranchModel>(){});

    }

    public StepModel checkStep(StepInstance si) throws JsonProcessingException {
        return getExtendedObjectMapper().readValue(si.getVars(), new TypeReference<StepModel>(){});

    }

    public StepModel checkStep(QueueStorage task) throws JsonProcessingException {
        return getStepModel(task.getParameters());
    }

    public StepModel getStepModel(String vars) throws JsonProcessingException {
        return getExtendedObjectMapper().readValue(vars, new TypeReference<StepModel>(){});
    }

    public List<String> execBranchGroovyScript(StepInstance si ) throws JsonProcessingException {
        String sVars = si.getEtlInstance().getEtlVars();
        if (StringUtils.isNotEmpty(sVars)) {
            Map<String, Object> vars = stringToJsonVar(sVars);
            Binding binding = new Binding();
            binding.setVariable("stepName", si.getName());
            binding.setVariable("vars", vars);
            GroovyShell shell = new GroovyShell(binding);
            Object oResult = shell.evaluate(si.getStep().getBranchCondition());
            return stringBranchVars(oResult);

        }
        return null;
    }

    public  boolean execSensorBranchGroovyScript(StepInstance si ) throws JsonProcessingException {
        SensorModel sm = checkSensor(si);
        Map<String, Object> vars = stringToJsonVar(si.getVars());
        Map<String, Object> results = stringToJsonVar(si.getEtlInstance().getEtlVars());
        Binding binding = new Binding();
        binding.setVariable("stepName", si.getName());
        binding.setVariable("vars", vars);
        binding.setVariable("results", results);
        GroovyShell shell = new GroovyShell(binding);
        Object oResult = shell.evaluate(si.getStep().getBranchCondition());

        return  false;//stringBranchVars(oResult);

    }

    public static List<String> stringBranchVars(Object o) throws JsonProcessingException {
        if (o==null){
            return null;
        }
        String s = o.toString();
        if (s.isEmpty()){
            return null;
        }
        TypeReference<List<String>> typeRef = new TypeReference<>() { };
        return getObjectMapper().readValue(s, typeRef);
    }

    public String calculateResult(String taskLog, StepModel sModel, QueueStorage data) throws JsonProcessingException {
        String result=null;
        if (Boolean.TRUE.equals(data.getSaveCalculate())) {
            Binding binding = new Binding();
            binding.setVariable("taskLog", taskLog);
            binding.setVariable("vars", sModel.getVars());
            GroovyShell shell = new GroovyShell(binding);
            String  oResult =  shell.evaluate(sModel.getLogToTesultScript()).toString();

            // parse result and put one to declared var
            ObjectMapper objectMapper= getObjectMapper();
            objectMapper.readValue(oResult, HashMap.class);
            result = oResult.toString();
            Map<String,Object> res = new HashMap<>(1);
            res.put("result",result);
            data.setEtlVars(objectMapper.writeValueAsString(res));
            // save one
            queueStorageRepository.saveAndFlush(data);

        }
        return result;
    }



/*
    protected String applyVars(String property, Map<String,Object> vars){
        if (property==null){
            return null;
        }
        String result = property;
        for (Map.Entry<String, Object> entry: vars.entrySet()){
            String template = "\\$\\{" + entry.getKey() + "\\}";

            if(entry.getValue().toString().toUpperCase().startsWith("ENV")){
                String varTemplate =  entry.getValue().toString().replaceAll("ENV.","");
                log.info("varTemplate : {}", varTemplate);
                String varValue= env.getProperty(varTemplate);
                log.info("varValue : {}", varValue);
                if (varValue!=null) {
                    result = result.replaceAll(template, varValue);
                }else{
                    log.warn("varValue : is null");
                }
            }else {
                result = result.replaceAll(template, entry.getValue().toString());
            }
        }
        return result;
    }

 */
}
