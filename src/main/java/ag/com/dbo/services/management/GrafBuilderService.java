package ag.com.dbo.services.management;

import ag.com.dbo.controllers.model.StepData;
import ag.com.dbo.models.graf.Figure;
import ag.com.dbo.models.graf.Line;
import ag.com.dbo.models.graf.Rectangle;
import ag.com.dbo.models.management.EtlDto;
import ag.com.dbo.models.management.EtlInstance;
import ag.com.dbo.models.management.Step;
import ag.com.dbo.models.management.StepInstance;
import ag.com.dbo.repositories.management.EtlInstanceRepository;
import ag.com.dbo.repositories.management.StepInstanceRepository;
import ag.com.dbo.repositories.management.StepRepository;
import ag.com.dbo.services.management.impl.EtlServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface GrafBuilderService {



    /**
     *  get full stepDates for etl
     * @param etlId
     * @return
     */

    StepData  getStepsField(BigInteger etlId) ;

    /**
     * get all steps for etl
     * @param etlId
     * @return
     */
    List<Step> getSteps(BigInteger etlId);


}
