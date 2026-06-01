package ag.com.dbo.controllers;

import ag.com.dbo.controllers.model.StepData;
import ag.com.dbo.services.management.GrafBuilderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigInteger;

@Slf4j
@Controller
public class StepsFieldController {

    private final GrafBuilderService grafBuilderService;

    public StepsFieldController(GrafBuilderService grafBuilderService) {
        this.grafBuilderService = grafBuilderService;
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
