package ag.com.dbo.service;


import ag.com.dbo.models.management.StepInstance;
import ag.com.dbo.services.Utils;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Transactional

public class BranchUtilServiceTest {

    @Test
    public void testEmptyData() throws IOException {
        Resource resource = new ClassPathResource("testData/serviceBranchTest1.json");
        String content = resource.getContentAsString(StandardCharsets.UTF_8);
        StepInstance si = new StepInstance();
        si.setVars(content);
        si.setName("start");
        List<String> branches= Utils.getCorrectBranches(si);
        assertEquals(2, branches.size());
        assertEquals("a", branches.get(0));
        assertEquals("b", branches.get(1));

    }

}
