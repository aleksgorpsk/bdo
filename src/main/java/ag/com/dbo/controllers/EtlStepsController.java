package ag.com.dbo.controllers;

import ag.com.dbo.models.graf.Figure;
import ag.com.dbo.models.management.StepDTO;
import ag.com.dbo.models.management.EtlDto;
import ag.com.dbo.models.management.Etl;
import ag.com.dbo.repositories.management.StepRepository;
import ag.com.dbo.services.management.impl.EtlServiceImpl;
import ag.com.dbo.services.management.impl.GrafBuilderServiceImpl;
import ag.com.dbo.services.management.impl.StepServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
public class EtlStepsController {

    private final GrafBuilderServiceImpl grafBuilderService;
    private final StepRepository stepRepository;
    private final StepServiceImpl stepService;
    private final EtlServiceImpl etlService;


    public EtlStepsController(GrafBuilderServiceImpl grafBuilderService, StepRepository stepRepository, StepServiceImpl stepService, EtlServiceImpl etlService) {
        this.grafBuilderService = grafBuilderService;
        this.stepRepository = stepRepository;
        this.stepService = stepService;
        this.etlService = etlService;
    }

    @GetMapping("etl_step/{etlId}")
  public String getAll(@PathVariable("etlId") String etlId, Model model) {
        log.info("Step1: id {} model{}", etlId, model);
        BigInteger   bint = new BigInteger(etlId);
        List<Figure> b = grafBuilderService.getFigures(bint) ;

        model.addAttribute("data", b);
        model.addAttribute("etlId", etlId);

        return "etl_graf";
    }


    @PostMapping("/etlstep/save")
    public String saveEtl(StepDTO stepDto, RedirectAttributes redirectAttributes) {
        try {
           Optional<EtlDto>etlDTO = etlService.findById(stepDto.getEtlId());
            etlDTO.ifPresent(dto -> stepDto.setEtl(etlService.mapFrom(dto)));
            if (stepDto.getStepId()!=null) {
                stepService.update(stepDto);
            }else{
                stepService.create(stepDto);
            }
            redirectAttributes.addFlashAttribute("message", "The Step has been saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addAttribute("message", e.getMessage());
        }

        return "redirect:/etl_step/"+stepDto.getEtl().getId();
    }

    @GetMapping("/step/new/{etlId}")
    public String addEtlStep(
            @PathVariable("etlId") BigInteger etlId,
            Model model) {
        Optional<EtlDto> etlDto =etlService.findById(etlId);
        StepDTO step = new StepDTO();
        step.setMaxAttempts(2);
        if(etlDto.isPresent()) {
            Etl etl= etlService.mapFrom( etlDto.get());
            step.setEtl(etl);
        }
        step.setStepActive(true);
        model.addAttribute("etlId", etlId);
        model.addAttribute("step", step);
        model.addAttribute("pageTitle", "Create new Etl Step");
        model.addAttribute("allSteps", stepRepository.findAllStepsByEtl(etlId));
        return "etl_step_form";
    }
//    etl_step/step/edit/43
    @GetMapping("/etl_step/edit/{stepId}")
    public String editEtlStep(
            @PathVariable("stepId") BigInteger stepId,
            Model model) {

        StepDTO stepDto =stepService.retrieveById(stepId);

        model.addAttribute("etlId", stepId);
        model.addAttribute("step", stepDto);
        model.addAttribute("pageTitle", "Edit Etl stepId");
        model.addAttribute("allSteps", stepRepository.findAllStepsByEtl(stepDto.getEtl().getId()));
//        model.addAttribute("stepTypeList", stepDto.getStepTypeList());

        return "etl_step_form";
    }

    @GetMapping("/etl_step/delete/{stepId}")
    public String delete(
            @PathVariable("stepId") BigInteger stepId,
            Model model) {
        StepDTO step = stepService.retrieveById(stepId);
        stepService.delete(stepId);
        log.info("Deleet:{}",stepId);
        if (step==null) {
            return "etl_browser";
        }
         return "redirect:/etl_step/" + step.getEtl().getId();

    }
    /*
     * -------------------------------------------------------------------------
     * Update
     * -------------------------------------------------------------------------
     */

    public boolean update(StepDTO stepDTO) {
        if (stepRepository.existsById(stepDTO.getStepId())) {
            stepRepository.save(stepService.mapFrom(stepDTO));
            return true;
        } else {
            return false;
        }
    }

}
