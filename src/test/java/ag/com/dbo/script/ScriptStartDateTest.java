package ag.com.dbo.script;



import ag.com.dbo.controllers.model.ScriptResponse;
import ag.com.dbo.models.script.Script;
import ag.com.dbo.models.script.ScriptType;
import ag.com.dbo.repositories.management.NodeRepository;
import ag.com.dbo.repositories.management.ScriptRepository;
import ag.com.dbo.repositories.management.StepInstanceRepository;
import ag.com.dbo.services.management.ExternalService;
import ag.com.dbo.services.script.GroovyService;
import ag.com.dbo.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@Slf4j
@SpringBootTest
@TestPropertySource(properties = { "server.port=9081",    "myapp.timeout=5000"})
@ActiveProfiles("test")
@Transactional
public class ScriptStartDateTest {

    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private StepInstanceRepository stepInstanceRepository;

    private ExternalService getExternalService(){
        RestClient scriptRestClient = Mockito.mock(RestClient.class);
        ScriptRepository scriptRepository =Mockito.mock(ScriptRepository.class);
        ExternalService externalService = new ExternalService(this.stepInstanceRepository,
                this.nodeRepository,
                scriptRepository);
        externalService.port="9051";
        return  externalService;
    }

    @Test
    public void testStartJsonData() throws IOException {

        Resource resource = new ClassPathResource("testData/StartDateTime.json");
        String varsContent = resource.getContentAsString(StandardCharsets.UTF_8);

        GroovyService service = new GroovyService();
        String scripText= """
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

def y = ZonedDateTime.parse(vars.etl.startEtl)
def formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
Map.of("partitionPrefix", y.format(formatter))
                """;

        String language;
        String name;
        String type;
        String version;
//        Script script = new Script( new BigInteger("-2")  , "GROOVY","test", ScriptType.Common.name(),"1.1",scripText,Boolean.TRUE);
        Script script = new Script( new BigInteger("-2")  , "GROOVY","test","1.1", scripText, Boolean.TRUE);
        ScriptResponse resp= service.execGroovyScript(script , varsContent, "", "",   "test");
        log.info("ops!");
        assertEquals(Constants.OK, resp.getStatus());
        assertEquals("{\"partitionPrefix\":\"20260730\"}", resp.getResponse());

    }
}
