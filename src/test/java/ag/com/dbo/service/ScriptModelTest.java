package ag.com.dbo.service;

import ag.com.dbo.models.checker.script.DirtyScriptModel;

import ag.com.dbo.models.checker.script.ScriptInfo;
import ag.com.dbo.models.checker.script.ScriptModel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static ag.com.dbo.utils.Utils.getExtendedObjectMapper;
import static org.junit.Assert.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Transactional

public class ScriptModelTest {


    @Test
    public void testScriptTemplateTest1() throws IOException {
        Resource resource = new ClassPathResource("testData/ScriptTemplateTest1.json");
        String content = resource.getContentAsString(StandardCharsets.UTF_8);

        DirtyScriptModel model = getExtendedObjectMapper().readValue(content, new TypeReference<>(){});

        assertEquals(2, model.getScripts().size());
        assertEquals("testBranch3", model.getScripts().get(0).getScriptName());
        assertEquals("result1", model.getScripts().get(0).getResultName());
        assertEquals("testBranch4", model.getScripts().get(1).getScriptName());
        assertEquals("result4", model.getScripts().get(1).getResultName());
        assertEquals("/Users/aleksgor/opt/data", model.getProperties().get("directoryName"));

    }

    @Test
    public void testScriptTemplateTest2() throws IOException {
        Resource resource = new ClassPathResource("testData/ScriptTemplateTest1.json");
        String content = resource.getContentAsString(StandardCharsets.UTF_8);

        DirtyScriptModel model = getExtendedObjectMapper().readValue(content, new TypeReference<>(){});
        ScriptModel sm = model.getScriptModel("testBranch3");


        assertEquals("/Users/aleksgor/opt/data", sm.getProperties().get("directoryName"));
        assertEquals("result1", model.getScriptModel("testBranch3").getResultName());

    }


    @Test
    public void testScriptTemplateTest3() throws IOException {


        Resource resource = new ClassPathResource("testData/DirtyScript.json");
        String content = resource.getContentAsString(StandardCharsets.UTF_8);

        DirtyScriptModel model = getExtendedObjectMapper().readValue(content, new TypeReference<>(){});
        ScriptModel sm = model.getScriptModel("testBranch3");


        assertEquals("/Users/aleksgor/opt/data", sm.getProperties().get("directoryName"));
        assertEquals("result1", model.getScriptModel("testBranch3").getResultName());

    }

}
