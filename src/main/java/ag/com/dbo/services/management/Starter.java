package ag.com.dbo.services.management;

import ag.com.dbo.controllers.FullEtlInstance;
import ag.com.dbo.controllers.model.ScriptResponse;
import ag.com.dbo.models.checker.ModelName;
import ag.com.dbo.models.management.Etl;
import ag.com.dbo.models.management.EtlInstance;
import ag.com.dbo.models.management.StepInstance;
import ag.com.dbo.models.script.ScriptDefinition;

import java.util.List;

public interface Starter {
    /**
     *  start Elt
     * @param etl
     * @param schedule
     */
    void startEtl(Etl etl , boolean schedule) ;

    /**
     *  Make Instances from etl
     * @param etl
     */
    void createStepInstances(EtlInstance etl);

    /**
     * continue etl instance (second part)
     * @param si
     */
    void stepFrom(StepInstance si) ;

    /**
     * Start script
     * @param si
     * @param script
     * @return
     */
    ScriptResponse runOneScript(StepInstance si, ScriptDefinition script) ;

    /**
     * calculate logic( parent statuses ) for run script
     * @param si
     * @param scriptType
     * @return
     */
    ScriptResponse runScripts(StepInstance si, ModelName scriptType) ;

    /**
     * set  status 'Missed' if needed
     * @param fullEtlInstance
     * @param stepInstanceId
     * @param recursive
     * @return
     */
    List<StepInstance> recursiveSetMissed(FullEtlInstance fullEtlInstance, String stepInstanceId, boolean recursive) ;

    /**
     * Send script to queue
     * @param currentSi
     * @param fullEtlInstance
     */
    void enqueueTask(StepInstance currentSi, FullEtlInstance fullEtlInstance) ;

    }
