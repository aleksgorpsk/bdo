package ag.com.dbo.service;

import ag.com.dbo.models.checker.SensorModel;
import ag.com.dbo.models.management.StepInstance;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static ag.com.dbo.services.Utils.getSensorModel;
import static org.junit.Assert.assertEquals;
import static  ag.com.dbo.services.Utils.addVarToStep;


@SpringBootTest
@ActiveProfiles("test")

public class UtilsModelTest {


    @Test
    public void testEmptyData() throws JsonProcessingException {
        /*
         attemptTimeOut; // sec
    private Long failTimeout;
         */
        StepInstance step = new StepInstance();
        addVarToStep(step, "attemptTimeOut", 10L);
        addVarToStep(step, "failTimeout", 100L);
        SensorModel sm = getSensorModel(step.getVars());
        assertEquals(Long.valueOf(10), sm.getAttemptTimeOut());
        assertEquals(Long.valueOf(100), sm.getFailTimeout());

    }


}
