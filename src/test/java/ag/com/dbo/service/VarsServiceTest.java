package ag.com.dbo.service;

import ag.com.dbo.models.checker.ModelName;
import ag.com.dbo.models.checker.varmodel.*;
import ag.com.dbo.models.checker.ModelParser;
import org.json.JSONException;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;


import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;


@SpringBootTest
@ActiveProfiles("test")

public class VarsServiceTest {


    @Test
    public void varTest() throws IOException, JSONException {
        Resource resource = new ClassPathResource("testData/varTest.json");
        String varsContent = resource.getContentAsString(StandardCharsets.UTF_8);

        ModelParser modelFactory = new ModelParser(varsContent);
        BranchModel model= modelFactory.getBranch();
        assertEquals("GROOVY:test1:11", model.getScriptName());
        assertEquals(441L, model.getFailTimeout().longValue());
    }

    @Test
    public void varSensorTest() throws IOException, JSONException {
        Resource resource = new ClassPathResource("testData/varTest.json");
        String varsContent = resource.getContentAsString(StandardCharsets.UTF_8);
        ModelParser modelFactory = new ModelParser(varsContent);
        SensorModel model= modelFactory.getSensor();
        assertEquals("GROOVY:test2:12", model.getScriptName());
        assertEquals(33, model.getAttemptTimeOut().longValue());
        assertEquals(442, model.getFailTimeout().longValue());

    }

    @Test
    public void varPrepareDataTest() throws IOException, JSONException {
        Resource resource = new ClassPathResource("testData/varTest.json");
        String varsContent = resource.getContentAsString(StandardCharsets.UTF_8);

        ModelParser modelFactory = new ModelParser(varsContent);
        PrepareDataModel model= modelFactory.getPrepareDataModel();
        assertEquals("GROOVY:test4:14", model.getScriptName());
        assertEquals(444L, model.getTimeout().longValue());
    }

    @Test
    public void varShellCommandTest() throws IOException, JSONException {
        Resource resource = new ClassPathResource("testData/varTest.json");
        String varsContent = resource.getContentAsString(StandardCharsets.UTF_8);

        ModelParser modelFactory = new ModelParser(varsContent);
        ShellCommandModel model= modelFactory.getShellCommand();
        assertEquals("GROOVY:test3:13", model.getScriptName());
        assertEquals(22L, model.getFailTimeout().longValue());
        assertEquals("test", model.getResultName());
    }

    @Test
    public void varPostProcessTest() throws IOException, JSONException {
        Resource resource = new ClassPathResource("testData/varTest.json");
        String varsContent = resource.getContentAsString(StandardCharsets.UTF_8);

        ModelParser modelFactory = new ModelParser(varsContent);
        PostProcessModel model= modelFactory.getPostProcessModel();
        assertEquals("GROOVY:test5:15", model.getScriptName());
        assertEquals("ttest", model.getResultName());
        assertEquals(445L, model.getTimeout().longValue());

    }

    @Test
    public void varSkipSensorTest() throws IOException, JSONException {
        Resource resource = new ClassPathResource("testData/varTest2.json");
        String varsContent = resource.getContentAsString(StandardCharsets.UTF_8);
        ModelParser modelFactory = new ModelParser(varsContent);
        SensorModel model= modelFactory.getSensor();
        assertNull(model);
    }


    @Test
    public void varSkipVarShellCommandTest() throws IOException, JSONException {
        Resource resource = new ClassPathResource("testData/varTest2.json");
        String varsContent = resource.getContentAsString(StandardCharsets.UTF_8);
        ModelParser modelFactory = new ModelParser(varsContent);
        ShellCommandModel model= modelFactory.getShellCommand();
        assertEquals("GROOVY:test3:13", model.getScriptName());
        assertEquals(1L, model.getFailTimeout().longValue());
        assertEquals("test", model.getResultName());
    }

    @Test
    public void getModelTest() throws IOException, JSONException {
        Resource resource = new ClassPathResource("testData/varTest2.json");
        String varsContent = resource.getContentAsString(StandardCharsets.UTF_8);
        ModelParser modelFactory = new ModelParser(varsContent);
        CommonModel cm = modelFactory.getModel(ModelName.branchScript);
        assertNotNull(cm);
        assertEquals("GROOVY:test1:11", cm.getScriptName());

        CommonModel cm1 = modelFactory.getModel(ModelName.sensorScript);
        assertNull(cm1);

        CommonModel cm2 = modelFactory.getModel(ModelName.shellCommandScript);
        assertNotNull(cm2);
        assertEquals("GROOVY:test3:13", cm2.getScriptName());

        CommonModel cm3 = modelFactory.getModel(ModelName.prepareScript);
        assertNotNull(cm3);
        assertEquals("GROOVY:test4:14", cm3.getScriptName());

        CommonModel cm4 = modelFactory.getModel(ModelName.postProcessScript);
        assertNotNull(cm4);
        assertEquals("GROOVY:test5:15", cm4.getScriptName());

    }
}
