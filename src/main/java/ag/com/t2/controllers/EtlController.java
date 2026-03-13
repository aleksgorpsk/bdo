package ag.com.t2.controllers;

import ag.com.t2.models.Etl;
import ag.com.t2.repositories.EtlRepository;
import ag.com.t2.services.EtlService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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

}
