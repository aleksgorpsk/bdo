package ag.com.dbo.controllers;

import ag.com.dbo.models.StepInstance;
import ag.com.dbo.repositories.StepInstanceRepository;
import ag.com.dbo.services.EngineService;
import ag.com.dbo.services.loadingService.MultithreadExecutor;
import ag.com.dbo.services.loadingService.model.PropData;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RestController
@Tag(name = "Executor")
@Slf4j
@OpenAPIDefinition(info = @Info(title = "java.samples.spring.Executor", version = "1.0", description = "Proof of Concept for a RESTful Web Service made with JDK 21 (LTS) and Spring Boot 3", contact = @Contact(name = "GitHub", url = "https://github.com/nanotaboada/java.samples.spring.boot"), license = @License(name = "MIT License", url = "https://opensource.org/licenses/MIT")))
public class ExecutorController {

    private final MultithreadExecutor executor;
    private final StepInstanceRepository stepInstanceRepository;
    private final EngineService engineService;

    public ExecutorController(MultithreadExecutor executor,
                              StepInstanceRepository stepInstanceRepository, EngineService engineService) {
        this.executor = executor;
        this.stepInstanceRepository = stepInstanceRepository;
        this.engineService = engineService;
    }

    /*
     * -------------------------------------------------------------------------
     * HTTP POST
     * -------------------------------------------------------------------------
     */

    @PostMapping("/exece")
    @Operation(summary = "executor")
    @ApiResponses(value = {
    })
    public ResponseEntity<@NonNull PropData> post(@RequestBody  String data ) {
        BigInteger id = new BigInteger(data);
        StepInstance si = stepInstanceRepository.getReferenceById(id);
        PropData result =  executor.exec(si);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/schedule")
    @Operation(summary = "schedule")
    public  ResponseEntity<@NonNull String>  schedule( ) {
        engineService.schedule();
        return ResponseEntity.ok("Run");
    }

}
