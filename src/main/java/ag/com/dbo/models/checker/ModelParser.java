package ag.com.dbo.models.checker;

import ag.com.dbo.models.checker.varmodel.*;
import ag.com.dbo.models.checker.varmodel.CommonModel;
import ag.com.dbo.models.script.ScriptDefinition;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ag.com.dbo.services.Utils.getScriptDefinitionByName;

public class ModelParser {
    private final JSONObject jSONObject;
    private final Map<String, CommonModel> namedModel = new HashMap<>();

    public ModelParser(String var) {
        this.jSONObject = new JSONObject(var);
        paresAll();
    }

    public CommonModel getBranch() {
        CommonModel result = new CommonModel();
        if (jSONObject.has(ModelName.branchScript.name())) {
            JSONObject branch = jSONObject.getJSONObject(ModelName.branchScript.name());
            result.setFailTimeout(branch.has("failTimeout") ? branch.getLong("failTimeout") : 3600);
            result.setScriptName(branch.getString("scriptName"));
            result.setScriptDefinition(getScriptDefinitionByName(result.getScriptName()));
            result.setModelName(ModelName.branchScript);
            return result;
        } else {
            return null;
        }
    }

    public CommonModel getSensor() {
        CommonModel result = new CommonModel();
        if (jSONObject.has(ModelName.sensorScript.name())) {
            JSONObject sensor = jSONObject.getJSONObject(ModelName.sensorScript.name());
            result.setAttemptTimeOut(sensor.has("attemptTimeOut") ? sensor.getLong("attemptTimeOut") : 600);
            result.setFailTimeout(sensor.has("failTimeout") ? sensor.getLong("failTimeout") : 3600);
            result.setScriptName(sensor.getString("scriptName"));
            result.setScriptDefinition(getScriptDefinitionByName(result.getScriptName()));
            result.setModelName(ModelName.sensorScript);
            return result;
        } else {
            return null;
        }
    }

    public CommonModel getPrepareDataModel() {
        CommonModel result = new CommonModel();
        if (jSONObject.has(ModelName.prepareScript.name())) {
            JSONObject pd = jSONObject.getJSONObject(ModelName.prepareScript.name());
            result.setTimeout(pd.has("timeout") ? pd.getLong("timeout") : 1);
            result.setScriptName(pd.getString("scriptName"));
            result.setScriptDefinition(getScriptDefinitionByName(result.getScriptName()));
            result.setModelName(ModelName.prepareScript);
            return result;
        } else {
            return null;
        }

    }

    public CommonModel getShellCommand() {
        CommonModel result = new CommonModel();
        if (jSONObject.has(ModelName.shellCommandScript.name())) {
            JSONObject shellCommand = jSONObject.getJSONObject(ModelName.shellCommandScript.name());
            result.setFailTimeout(shellCommand.has("failTimeout") ? shellCommand.getLong("failTimeout") : 1L);
            result.setScriptName(shellCommand.getString("scriptName")); // must have
            result.setResultName(shellCommand.has("resultName") ? shellCommand.getString("resultName") : "shellResult");
            result.setMaxAttempts(shellCommand.has("maxAttempts") ? shellCommand.getInt("maxAttempts") : 2);
            result.setScriptDefinition(getScriptDefinitionByName(result.getScriptName()));
            result.setModelName(ModelName.shellCommandScript);
            return result;
        } else {
            return null;
        }

    }

    public CommonModel getPostProcessModel() {
        CommonModel result = new CommonModel();
        if (jSONObject.has(ModelName.postProcessScript.name())) {
            JSONObject ppm = jSONObject.getJSONObject(ModelName.postProcessScript.name());
            result.setTimeout(ppm.getLong("timeout"));
            result.setScriptName(ppm.getString("scriptName"));
            result.setResultName(ppm.has("scriptName") ? ppm.getString("resultName") : "postProcess");
            result.setScriptDefinition(getScriptDefinitionByName(result.getScriptName()));
            result.setModelName(ModelName.postProcessScript);
            return result;
        } else {
            return null;
        }
    }

    private void paresAll() {
        List<String> modelNames = Arrays.stream(ModelName.values()).map(Enum::name).toList();
        for (String modelName : modelNames) {
            if (jSONObject.has(modelName)) {
                if (ModelName.branchScript.name().equals(modelName)) {
                    namedModel.put(modelName, getBranch());
                } else if (ModelName.sensorScript.name().equals(modelName)) {
                    namedModel.put(modelName, getSensor());
                } else if (ModelName.shellCommandScript.name().equals(modelName)) {
                    namedModel.put(modelName, getShellCommand());
                } else if (ModelName.prepareScript.name().equals(modelName)) {
                    namedModel.put(modelName, getPrepareDataModel());
                } else if (ModelName.postProcessScript.name().equals(modelName)) {
                    namedModel.put(modelName, getPostProcessModel());
                }
            }
        }
    }

    public CommonModel getModelByScriptDefinition(ScriptDefinition scriptDefinition){
        List<CommonModel> models= namedModel.values().stream().filter(x-> scriptDefinition.equals(x.getScriptDefinition())).toList();
        if(models.size()==1){
            return models.get(0);
        }
        return null;
    }

    public CommonModel getModel(ModelName modelType){
        return namedModel.getOrDefault(modelType.name(),null);
    }

    public boolean isSensor(){
        return namedModel.containsKey(ModelName.sensorScript.name());
    }
}
