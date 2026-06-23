package ag.com.dbo.controllers;

import ag.com.dbo.models.graf.Figure;
import ag.com.dbo.models.management.*;
import ag.com.dbo.repositories.management.DataLoadingRepository;
import ag.com.dbo.repositories.management.StepRepository;
import ag.com.dbo.services.management.EtlService;
import ag.com.dbo.services.management.GrafBuilderService;
import ag.com.dbo.services.management.StepService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
public class EtlStepsController {

    private final GrafBuilderService grafBuilderService;
    private final StepRepository stepRepository;
    private final StepService stepService;
    private final EtlService etlService;
    private final DataLoadingRepository dataLoadingRepository;


    public EtlStepsController(GrafBuilderService grafBuilderService, StepRepository stepRepository, StepService stepService, EtlService etlService, DataLoadingRepository dataLoadingRepository) {
        this.grafBuilderService = grafBuilderService;
        this.stepRepository = stepRepository;
        this.stepService = stepService;
        this.etlService = etlService;
        this.dataLoadingRepository = dataLoadingRepository;
    }

    @GetMapping("etl_step/{etlId}")
  public String getAll(@PathVariable("etlId") String etlId, Model model) {
        log.info("Step1: id {} model{}", etlId, model);
        BigInteger   bint = new BigInteger(etlId);
        List<Figure> b = grafBuilderService.getFigures(bint) ;

//        ModelAndView mav = new ModelAndView("etl_steps"); // sets view name
        model.addAttribute("data", b);
        model.addAttribute("etlId", etlId);

        return "etl_graf";
    }


    @PostMapping("/etlstep/save")
    public String saveEtl(StepDTO stepDto, RedirectAttributes redirectAttributes) {
        try {
            if (StepType.Sensor.name().equals(stepDto.getStepType())
                    || StepType.Branch.name().equals(stepDto.getStepType())){
                ;
            }else{
                stepDto.setStepType(null);
            }
           Optional<EtlDTO>etlDTO = etlService.findById(stepDto.getEtlId());
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

        return "redirect:/etl_browser";
    }

    @GetMapping("/step/new/{etlId}")
    public String addEtlStep(
            @PathVariable("etlId") BigInteger etlId,
            Model model) {
        Optional<EtlDTO> etlDto =etlService.findById(etlId);

        Step step = new Step();
        if(etlDto.isPresent()) {
            Etl etl= etlService.mapFrom( etlDto.get());
            step.setEtl(etl);
        }
        step.setStepActive(true);
        log.info("!!!!!etlId:{}", etlId);
//        step.setEtl(etlId);
        model.addAttribute("etlId", etlId);
        model.addAttribute("step", step);
        model.addAttribute("pageTitle", "Create new Etl Step");
        model.addAttribute("allsteps", stepRepository.findAllStepsByEtl(etlId));
        model.addAttribute("allDataLoading", dataLoadingRepository.findAll());
        List<String> types = Arrays.stream(StepType.values()).map(Enum::name).toList();
        model.addAttribute("stepTypes", types);


        return "etl_step_form";
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
