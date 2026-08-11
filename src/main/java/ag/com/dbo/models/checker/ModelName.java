package ag.com.dbo.models.checker;


import ag.com.dbo.models.checker.varmodel.*;
import ag.com.dbo.models.checker.varmodel.SensorModel;

public enum ModelName {

    branchScript(BranchModel.class),
    sensorScript(SensorModel.class),
    shellCommandScript(ShellCommandModel.class),
    prepareScript(PrepareDataModel.class),
    postProcessScript(PostProcessModel.class);

    private final Class clazz;
    ModelName(Class clazz){
        this.clazz= clazz;
    }
    public Class getModelClass(){
          return this.clazz;
    }
}
