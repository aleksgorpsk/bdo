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

import static ag.com.dbo.services.Utils.getSensor;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;


@SpringBootTest
@ActiveProfiles("test")

public class VarsServiceTest {


    @Test
    public void varTest() throws IOException, JSONException {
        Resource resource = new ClassPathResource("testData/varTest.json");
        String varsContent = resource.getContentAsString(StandardCharsets.UTF_8);

        ModelParser modelFactory = new ModelParser(varsContent);
        CommonModel model= modelFactory.getBranch();
        assertEquals("GROOVY:test1:11", model.getScriptName());
        assertEquals(441L, model.getFailTimeout().longValue());
        assertEquals(ModelName.branchScript, model.getModelName());


    }

    @Test
    public void varSensorTest() throws IOException, JSONException {
        Resource resource = new ClassPathResource("testData/varTest.json");
        String varsContent = resource.getContentAsString(StandardCharsets.UTF_8);
        ModelParser modelFactory = new ModelParser(varsContent);
        CommonModel model= modelFactory.getSensor();
        assertEquals("GROOVY:test2:12", model.getScriptName());
        assertEquals(33, model.getAttemptTimeOut().longValue());
        assertEquals(442, model.getFailTimeout().longValue());
        assertEquals(ModelName.sensorScript, model.getModelName());

    }

    @Test
    public void varPrepareDataTest() throws IOException, JSONException {
        Resource resource = new ClassPathResource("testData/varTest.json");
        String varsContent = resource.getContentAsString(StandardCharsets.UTF_8);

        ModelParser modelFactory = new ModelParser(varsContent);
        CommonModel model= modelFactory.getPrepareDataModel();
        assertEquals("GROOVY:test4:14", model.getScriptName());
        assertEquals(444L, model.getTimeout().longValue());
        assertEquals(ModelName.prepareScript, model.getModelName());

    }

    @Test
    public void varShellCommandTest() throws IOException, JSONException {
        Resource resource = new ClassPathResource("testData/varTest.json");
        String varsContent = resource.getContentAsString(StandardCharsets.UTF_8);

        ModelParser modelFactory = new ModelParser(varsContent);
        CommonModel model= modelFactory.getShellCommand();
        assertEquals("GROOVY:test3:13", model.getScriptName());
        assertEquals(22L, model.getFailTimeout().longValue());
        assertEquals("test", model.getResultName());
        assertEquals(ModelName.shellCommandScript, model.getModelName());

    }

    @Test
    public void varPostProcessTest() throws IOException, JSONException {
        Resource resource = new ClassPathResource("testData/varTest.json");
        String varsContent = resource.getContentAsString(StandardCharsets.UTF_8);

        ModelParser modelFactory = new ModelParser(varsContent);
        CommonModel model= modelFactory.getPostProcessModel();
        assertEquals("GROOVY:test5:15", model.getScriptName());
        assertEquals("ttest", model.getResultName());
        assertEquals(445L, model.getTimeout().longValue());
        assertEquals(ModelName.postProcessScript, model.getModelName());

    }

    @Test
    public void varSkipSensorTest() throws IOException, JSONException {
        Resource resource = new ClassPathResource("testData/varTest2.json");
        String varsContent = resource.getContentAsString(StandardCharsets.UTF_8);
        ModelParser modelFactory = new ModelParser(varsContent);
        CommonModel model= modelFactory.getSensor();
        assertNull(model);

    }


    @Test
    public void varSkipVarShellCommandTest() throws IOException, JSONException {
        Resource resource = new ClassPathResource("testData/varTest2.json");
        String varsContent = resource.getContentAsString(StandardCharsets.UTF_8);
        ModelParser modelFactory = new ModelParser(varsContent);
        CommonModel model= modelFactory.getShellCommand();
        assertEquals("GROOVY:test3:13", model.getScriptName());
        assertEquals(1L, model.getFailTimeout().longValue());
        assertEquals("test", model.getResultName());
        assertEquals(ModelName.shellCommandScript, model.getModelName());

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

    @Test
    public void getModelTest3() throws IOException, JSONException {
        Resource resource = new ClassPathResource("testData/varTest3.json");
        String varsContent = resource.getContentAsString(StandardCharsets.UTF_8);
        CommonModel cmModel = getSensor(varsContent);
        assertEquals(ModelName.sensorScript, cmModel.getModelName());

    }
}
