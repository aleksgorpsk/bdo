package ag.com.dbo.service;


import ag.com.dbo.models.management.Etl;
import ag.com.dbo.models.management.EtlInstance;
import ag.com.dbo.repositories.management.EtlInstanceRepository;
import ag.com.dbo.repositories.management.EtlRepository;
import ag.com.dbo.repositories.management.StepInstanceRepository;
import ag.com.dbo.repositories.management.StepRepository;
import ag.com.dbo.services.management.EngineService;
import ag.com.dbo.services.management.ExternalService;
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
        ScriptService scriptService = Mockito.mock(ScriptService.class);
        ExternalService externalService  = Mockito.mock(ExternalService.class);

        EngineServiceTest test = new EngineServiceTest(etlRepository,etlInstanceRepository,
                stepRepository,stepInstanceRepository,  scriptService, externalService);

        Etl etl = new Etl();
        test.startEtl(etl, true);
        assertNotNull(ei.getStart());
        ReadContext ctx = JsonPath.parse(ei.getEtlVars());
        String startDate = ctx.read("$.startEtl");
        assertNotNull(startDate);
        assertNotNull(OffsetDateTime.parse(startDate));
    }

    protected static class EngineServiceTest extends EngineService {
        public EngineServiceTest(EtlRepository etlRepository, EtlInstanceRepository etlInstanceRepository,
                                 StepRepository stepRepository, StepInstanceRepository stepInstanceRepository,
                                 ScriptService scriptService, ExternalService externalService) {


            super(etlRepository, etlInstanceRepository, stepRepository, stepInstanceRepository, scriptService, externalService);
        }

        public void createStepInstances(EtlInstance etl) {
            ei = etl;
        }
    }
}
