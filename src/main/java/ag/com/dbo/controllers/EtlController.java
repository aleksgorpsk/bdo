package ag.com.dbo.controllers;

import ag.com.dbo.models.Etl;
import ag.com.dbo.repositories.EtlRepository;
import ag.com.dbo.services.EtlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@Controller
public class EtlController {

    private final EtlService etlService;

    public EtlController(EtlRepository etlRepository, EtlService etlService) {
        this.etlService = etlService;
    }

    @GetMapping("/browser1")
    public String browser1( Model model) {
        Iterable<Etl> etls = etlService.retrieveAll();
        model.addAttribute("list", etls);
        return "browser1";    }

    @PostMapping("/etlForm")
    public String saveEtl(
            @ModelAttribute("etl") Etl model) {
        log.info("saveEtl"+model);

//        Iterable<Etl> etls = etlService.retrieveAll();
//        model.addAttribute("list", etls);
        return "browser1";
    }


    @GetMapping("/addEtlForm")
    public String browser2( Model model) {
        Etl etl = new Etl();
        etl.setName("Testname");
        model.addAttribute("etl",etl);

        return "addEtlForm";
    }

}
