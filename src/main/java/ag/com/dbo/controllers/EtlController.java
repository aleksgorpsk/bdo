package ag.com.dbo.controllers;

import ag.com.dbo.models.Etl;
import ag.com.dbo.models.EtlDTO;
import ag.com.dbo.models.EtlInstance;
import ag.com.dbo.models.EtlStatus;
import ag.com.dbo.repositories.EtlRepository;
import ag.com.dbo.services.EngineService;
import ag.com.dbo.services.EtlService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Controller
public class EtlController {

    private final EtlService etlService;
    private final EngineService engineService;

    public EtlController(EtlRepository etlRepository, EtlService etlService, EngineService engineService) {
        this.etlService = etlService;
        this.engineService = engineService;
    }


    @GetMapping("etl/start")
    public ModelAndView start(
            Model model,
            @RequestParam(value= "etlId", required = true ) BigInteger etlId
    ) {
        log.info("Manual start: {}", etlId);
        EtlDTO etlToStart = etlService.retrieveById(etlId);
        etlToStart.setStatus(EtlStatus.AutoStart.getStatus());
        if (etlService.update(etlToStart)) {
            log.info("Manual start dto: {}", etlToStart);
            EtlInstance etlInst = engineService.createEtlInstance(etlService.mapFrom(etlToStart));
            log.info("EtlInstance start : {}", etlInst);
        }else{
            log.error("ETL not found :{}", etlToStart);
        }
        return browseEtl(Optional.empty(), Optional.empty());

    }

    @GetMapping("etl/browser")
    public ModelAndView browser(
            Model model,
            @RequestParam(value= "page", required = false, defaultValue = "1" ) Optional<Integer> page,
            @RequestParam(value = "size",  required = false, defaultValue = "5") Optional<Integer> size
    ) {
        return  browseEtl(page, size);
    }

    public ModelAndView browseEtl(Optional<Integer> page, Optional<Integer> size){
        ModelAndView modelAndView = new ModelAndView("etlBrowser");
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(5);

        PageRequest pageable = PageRequest.of( currentPage-1, pageSize, Sort.by("Id").ascending());
        Page<@NonNull EtlDTO> etlPage = etlService.retrievePage(pageable);
        modelAndView.addObject("etlPage", etlPage);

        int totalPages = etlPage.getTotalPages();

        if(totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1,totalPages).boxed().collect(Collectors.toList());
            modelAndView.addObject("pageNumbers", pageNumbers);
        }
      //  modelAndView.addObject("activeEtlList", true);
        return modelAndView;
    }

    @GetMapping("etl/createEtlForm")
    public String createEtlForm( Model model) {
        model.addAttribute("etl",new Etl());
        return "addEtlForm";
    }


    @RequestMapping(value = "/etl/detail/{etlId}")
    public ModelAndView etlEditor(@PathVariable("etlId") BigInteger etlId) {
        ModelAndView modelAndView = new ModelAndView("etlEditor");
        EtlDTO etl = etlService.retrieveById(etlId);
        modelAndView.addObject(etl);
        modelAndView.addObject("etl",etl);
        log.info("Edit:{}", etl );
        return modelAndView;
    }
    @PostMapping("/etl/addAction")
    public ModelAndView createEtl(
            @ModelAttribute("etl") EtlDTO model) {
        log.info("addEtl"+model);
        etlService.create(model);
        return browseEtl(Optional.empty(), Optional.empty());
    }

    @PostMapping("etl/saveAction")
    public ModelAndView saveEtl(
            @ModelAttribute("etl") EtlDTO model) {
        log.info("save etl: {}", model);
        boolean success = etlService.update(model);

        log.info("save etl success: {}", success);

        return browseEtl(Optional.empty(), Optional.empty());
    }
}
