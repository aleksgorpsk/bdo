package ag.com.dbo.service;


import ag.com.dbo.models.management.Etl;
import ag.com.dbo.models.management.EtlInstance;
import ag.com.dbo.repositories.management.EtlInstanceRepository;
import ag.com.dbo.repositories.management.EtlRepository;
import ag.com.dbo.repositories.management.StepInstanceRepository;
import ag.com.dbo.repositories.management.StepRepository;
import ag.com.dbo.services.management.EngineService;
import ag.com.dbo.services.management.ExternalStepTypeService;
import ag.com.dbo.services.management.ScriptService;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Transactional

public class StartDatetimeTest {

    public static EtlInstance ei = new EtlInstance();
    @Test
    public void testEmptyData() throws IOException {

        EtlRepository etlRepository = Mockito.mock(EtlRepository.class);
        EtlInstanceRepository etlInstanceRepository= Mockito.mock(EtlInstanceRepository.class);
        StepRepository stepRepository =Mockito.mock(StepRepository.class);
        StepInstanceRepository stepInstanceRepository = Mockito.mock(StepInstanceRepository.class);
        ExternalStepTypeService externalStepTypeService= Mockito.mock(ExternalStepTypeService.class);
        ScriptService scriptService = Mockito.mock(ScriptService.class);

        EngineServiceTest test = new EngineServiceTest(etlRepository,etlInstanceRepository,
                stepRepository,stepInstanceRepository, externalStepTypeService, scriptService);

        Etl etl = new Etl();
        test.startEtl(etl);
        assertNotNull(ei.getStart());
        ReadContext ctx = JsonPath.parse(ei.getEtlVars());
        String startDate = ctx.read("$.startEtl");
        assertNotNull(startDate);
        assertNotNull(OffsetDateTime.parse(startDate));
    }
/*
 EtlRepository etlRepository, EtlInstanceRepository etlInstanceRepository,
            StepRepository stepRepository, StepInstanceRepository stepInstanceRepository,
            ExternalStepTypeService externalStepTypeService, ScriptService scriptService
 */
    protected static class EngineServiceTest extends EngineService {
        public EngineServiceTest(EtlRepository etlRepository, EtlInstanceRepository etlInstanceRepository,
                                 StepRepository stepRepository, StepInstanceRepository stepInstanceRepository,
                                 ExternalStepTypeService externalStepTypeService, ScriptService scriptService) {
            super(etlRepository, etlInstanceRepository, stepRepository, stepInstanceRepository, externalStepTypeService,scriptService);
        }

        public void createStepInstances(EtlInstance etl) {
            ei = etl;
        }
    }
}
