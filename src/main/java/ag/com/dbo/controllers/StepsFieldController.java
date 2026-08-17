package ag.com.dbo.controllers;

import ag.com.dbo.controllers.model.StepData;
import ag.com.dbo.repositories.management.StepRepository;
import ag.com.dbo.services.management.impl.EtlServiceImpl;
import ag.com.dbo.services.management.impl.GrafBuilderServiceImpl;
import ag.com.dbo.services.management.impl.StepServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigInteger;

@Slf4j
@Controller
public class StepsFieldController {

    private final GrafBuilderServiceImpl grafBuilderService;
    private final StepRepository stepRepository;
    private final StepServiceImpl stepService;
    private final EtlServiceImpl etlService;

    public StepsFieldController(GrafBuilderServiceImpl grafBuilderService, StepRepository stepRepository, StepServiceImpl stepService, EtlServiceImpl etlService) {
        this.grafBuilderService = grafBuilderService;
        this.stepRepository = stepRepository;
        this.stepService = stepService;
        this.etlService = etlService;
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
