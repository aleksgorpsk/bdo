package ag.com.dbo.controllers;

import ag.com.dbo.models.StepInstance;
import ag.com.dbo.repositories.StepInstanceRepository;
import ag.com.dbo.services.loadingService.MultithreadExecutor;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
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

    public ExecutorController(MultithreadExecutor executor,
                              StepInstanceRepository stepInstanceRepository) {
        this.executor = executor;
        this.stepInstanceRepository = stepInstanceRepository;
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
    public ResponseEntity<String> post(@RequestBody  String data ) {
        BigInteger id = new BigInteger(data);
        StepInstance si = stepInstanceRepository.getReferenceById(id);
        String result =  executor.exec(si);
        return ResponseEntity.ok(result);
    }
}
