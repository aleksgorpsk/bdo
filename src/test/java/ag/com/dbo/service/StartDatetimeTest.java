package ag.com.dbo.service;


import ag.com.dbo.models.management.Etl;
import ag.com.dbo.models.management.EtlInstance;
import ag.com.dbo.repositories.management.EtlInstanceRepository;
import ag.com.dbo.repositories.management.StepInstanceRepository;
import ag.com.dbo.repositories.management.StepRepository;
import ag.com.dbo.services.management.impl.EngineServiceImpl;
import ag.com.dbo.services.management.impl.ScriptServiceImpl;
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

        EtlInstanceRepository etlInstanceRepository= Mockito.mock(EtlInstanceRepository.class);
        StepRepository stepRepository =Mockito.mock(StepRepository.class);
        StepInstanceRepository stepInstanceRepository = Mockito.mock(StepInstanceRepository.class);
        ScriptServiceImpl scriptService = Mockito.mock(ScriptServiceImpl.class);

        EngineServiceTest test = new EngineServiceTest(etlInstanceRepository,
                stepRepository,stepInstanceRepository,  scriptService);

        Etl etl = new Etl();
        test.startEtl(etl, true);
        assertNotNull(ei.getStart());
        ReadContext ctx = JsonPath.parse(ei.getEtlVars());
        String startDate = ctx.read("$.startEtl");
        assertNotNull(startDate);
        assertNotNull(OffsetDateTime.parse(startDate));
    }
    protected static class EngineServiceTest extends EngineServiceImpl {
        public EngineServiceTest(EtlInstanceRepository etlInstanceRepository, StepRepository stepRepository,
                                 StepInstanceRepository stepInstanceRepository, ScriptServiceImpl scriptService) {


            super( etlInstanceRepository, stepRepository, stepInstanceRepository, scriptService);
        }

        public void createStepInstances(EtlInstance etl) {
            ei = etl;
        }
    }
}
