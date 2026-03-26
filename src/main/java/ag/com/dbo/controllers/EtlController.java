package ag.com.dbo.controllers;

import ag.com.dbo.models.Etl;
import ag.com.dbo.models.EtlDTO;
import ag.com.dbo.repositories.EtlRepository;
import ag.com.dbo.services.EtlService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Controller
public class EtlController {

    private final EtlService etlService;

    public EtlController(EtlRepository etlRepository, EtlService etlService) {
        this.etlService = etlService;
    }

    @GetMapping("etl/browser")
    public ModelAndView browser1( Model model) {
        return  browseEtl(null);
    }

    public ModelAndView browseEtl(Integer pageNumber){
        ModelAndView modelAndView = new ModelAndView("etlBrowser");
        if (pageNumber==null){
            pageNumber= 1;
        }
        PageRequest pageable = PageRequest.of( pageNumber-1, 10);
        Page<@NonNull EtlDTO> etlPage = etlService.retrievePage(pageable);
        int totalPages = etlPage.getTotalPages();
        if(totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1,totalPages).boxed().collect(Collectors.toList());
            modelAndView.addObject("pageNumbers", pageNumbers);
        }
        modelAndView.addObject("activeEtlList", true);
        modelAndView.addObject("etlList", etlPage.getContent());
        return modelAndView;
    }

    @GetMapping("etl/createEtlForm")
    public String createEtlForm( Model model) {
        model.addAttribute("etl",new Etl());
        return "addEtlForm";
    }

    @RequestMapping(value = "/etl-list/page/{page}")
    public ModelAndView listEtlPageByPage(@PathVariable("page") int page) {
        return browseEtl(page);
    }

    @RequestMapping(value = "/etl/detail/{etlId}")
    public ModelAndView etlEditor(@PathVariable("etlId") BigInteger etlId) {
        ModelAndView modelAndView = new ModelAndView("etlEditor");
        EtlDTO etl = etlService.retrieveById(etlId);
        modelAndView.addObject(etl);
        return modelAndView;
    }
    @PostMapping("/etl/addAction")
    public ModelAndView createEtl(
            @ModelAttribute("etl") EtlDTO model) {
        log.info("addEtl"+model);
        etlService.create(model);
        return browseEtl(null);
    }

    @PostMapping("etl/saveAction")
    public ModelAndView saveEtl(
            @ModelAttribute("etl") EtlDTO model) {
        log.info("save etl:"+model);
        etlService.update(model);

        return browseEtl(null);
    }
}
