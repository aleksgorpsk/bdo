package ag.com.dbo.services.management;

import ag.com.dbo.controllers.model.ScriptRequest;
import ag.com.dbo.models.checker.varmodel.CommonModel;
import ag.com.dbo.models.management.Etl;
import ag.com.dbo.models.management.Node;
import ag.com.dbo.models.management.StepInstance;
import ag.com.dbo.models.management.statuses.QueueInfo;
import ag.com.dbo.models.script.ScriptDefinition;
import ag.com.dbo.services.queue.model.PropData;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.client.RestClient;

import java.math.BigInteger;
import java.util.*;

public interface ExternalService {

    /**
     * prepare request  for  send to queue and send
     * @param si
     * @param scriptDefinition
     * @param shellCommandModel
     * @return
     */
     boolean  prepareToQueue(StepInstance si, ScriptDefinition scriptDefinition,  CommonModel shellCommandModel);

    /**
     * calculate and det RestClient
     * @return RestClient
     */
    RestClient getManager();

    /**
     * get Node clients
     * @return map id-> RestClient
     */
    Map<Integer, RestClient> getNodeClients();

    /**
     * prepare list of nodes
     */
    void afterPropertiesSet() ;

    /**
     * get Info about Nodes in Queue (busy, free spot )
     * @param node
     * @return
     */
    QueueInfo getInfo(Node node) ;

    /**
     * get worker client
     * @param host
     * @return
     */
    RestClient workerRestClient(String host) ;


    /**
     * get list of workers by tag
     * @param tags
     * @return
     */
    Node getHost(String tags) ;

    /**
     * send to queue
     * @param si
     * @param scriptDefinition
     * @param shellCommandModel
     * @return
     * @throws JsonProcessingException
     */

      boolean  sendToQueue(StepInstance si,  ScriptDefinition scriptDefinition, CommonModel shellCommandModel) throws JsonProcessingException ;

    /**
     *  send async
     * @param scriptRequest
     * @param si
     * @return
     * @throws JsonProcessingException
     */
    boolean sendTrToQueue(ScriptRequest scriptRequest, StepInstance si) throws JsonProcessingException ;

    /**
     * send to queue sync
     * @param testTaskRequest
     * @return
     * @throws JsonProcessingException
     */

    PropData sendTestReqst(ScriptRequest testTaskRequest) throws JsonProcessingException ;

    /**
     * delete etl from scheduling
     * @param etlId
     */
    public void deleteSchedule(BigInteger etlId);

    /**
     * update scheduling
     * @param etl
     */
    public void updateSchedule(Etl etl);

}
