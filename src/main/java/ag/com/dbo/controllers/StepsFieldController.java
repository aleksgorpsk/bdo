package ag.com.dbo.controllers;

import ag.com.dbo.controllers.model.StepData;
import ag.com.dbo.models.management.*;
import ag.com.dbo.repositories.management.DataLoadingRepository;
import ag.com.dbo.repositories.management.StepRepository;
import ag.com.dbo.services.management.EtlService;
import ag.com.dbo.services.management.GrafBuilderService;
import ag.com.dbo.services.management.StepService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigInteger;
import java.util.Optional;

@Slf4j
@Controller
public class StepsFieldController {

    private final GrafBuilderService grafBuilderService;
    private final StepRepository stepRepository;
    private final StepService stepService;
    private final EtlService etlService;
    private final DataLoadingRepository dataLoadingRepository;

    public StepsFieldController(GrafBuilderService grafBuilderService, StepRepository stepRepository, StepService stepService, EtlService etlService, DataLoadingRepository dataLoadingRepository) {
        this.grafBuilderService = grafBuilderService;
        this.stepRepository = stepRepository;
        this.stepService = stepService;
        this.etlService = etlService;
        this.dataLoadingRepository = dataLoadingRepository;
    }

    @GetMapping("/steps_field/{id}")
    public String getAll(@PathVariable("id") BigInteger id, Model model, RedirectAttributes redirectAttributes) {
        log.info("Step1: id {} model{}", id, model);

        StepData b = grafBuilderService.getStepsField(id);
//        model.addAttribute("data", b);

        model.addAttribute("data", b.getData());
        model.addAttribute("stepIds", b.getStepIds());
        model.addAttribute("etlInstanceIds", b.getEtlInstanceIds());
        model.addAttribute("etlInstances", b.getEtlInstances());
        model.addAttribute("steps", b.getSteps());

        return "steps_field";
    }

}
