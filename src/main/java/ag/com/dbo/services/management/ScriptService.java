package ag.com.dbo.services.management;

import ag.com.dbo.controllers.model.ScriptResponse;
import ag.com.dbo.models.management.StepInstance;
import ag.com.dbo.models.script.Script;
import ag.com.dbo.models.script.ScriptDefinition;
import ag.com.dbo.models.script.ScriptDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Optional;


@Service
public interface ScriptService {






    /**
     * run script on StepInstance
     * @param si
     * @return
     */
    ScriptResponse runScript(StepInstance si, ScriptDefinition scriptDefinition);

    /**
     * * script Ok/Not
     *
     * @param si
     * @return
     * @throws JsonProcessingException
     */
     ScriptResponse sendScript(StepInstance si, ScriptDefinition scriptId) throws JsonProcessingException;

    /**
     * sed script Sync
     * @param name
     * @param vars
     * @param localResult
     * @param etlVars
     * @param scriptDef
     * @return
     * @throws JsonProcessingException
     */
    ScriptResponse sendTestScript(String name , String vars, String localResult, String etlVars, ScriptDefinition scriptDef) throws JsonProcessingException ;

    //-- UI -----

    /**
     *  get page of ScriptDto
     * @param pageable
     * @return
     */
    Page<@NonNull ScriptDto> retrievePage(PageRequest pageable);

    /**
     * get page of ScriptDto
     * @param pageable
     * @return
     */
    public Page<@NonNull ScriptDto> retrievePage(Pageable pageable);

    /**
     * find by name
     * @param keyword
     * @param pageable
     * @return
     */
    public Page<@NonNull ScriptDto> findByScriptContainingIgnoreCase(String keyword, Pageable pageable);

    /**
     * update script
     * @param scriptDto
     * @return
     */
    public boolean update(ScriptDto scriptDto) ;

    /**
     * delete script
     * @param sid
     * @return
     */

    public boolean delete(BigInteger sid ) ;

    /**
     * get Optional<ScriptDto>  by id
     * @param id
     * @return
     */
    public Optional<ScriptDto> findById(BigInteger id);

    /**
     *  get Page with ScriptDto
     * @param script
     * @return
     */

    public Page<@NotNull ScriptDto> convert(Page<@NotNull Script> script);

    /**
     * get ScriptDto by id
     * @param sdto
     * @return
     */

    public Script getScript(ScriptDto sdto);


}
