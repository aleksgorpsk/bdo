package ag.com.dbo.controllers;

import ag.com.dbo.models.Etl;
import ag.com.dbo.repositories.EtlRepository;
import ag.com.dbo.services.EtlService;
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
