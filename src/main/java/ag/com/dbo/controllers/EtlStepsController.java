package ag.com.dbo.controllers;

import ag.com.dbo.models.graf.Figure;
import ag.com.dbo.models.graf.Line;
import ag.com.dbo.models.graf.Rectangle;
import ag.com.dbo.services.GrafBuilderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
public class EtlStepsController {

    private final GrafBuilderService grafBuilderService;

    public EtlStepsController(GrafBuilderService grafBuilderService) {
        this.grafBuilderService = grafBuilderService;
    }

    @GetMapping("/etl_step/{id}")
  public String getAll(@PathVariable("id") BigInteger id, Model model, RedirectAttributes redirectAttributes){
        log.info("Step1: id {} model{}",id, model);

        List<Figure> b = grafBuilderService.getFigures(id) ;
/*        List<Figure> a =new ArrayList<>();
        a.add(new Rectangle(10,10, 20,50, "red"));
        a.add(new Line(10,10, 100,100, "red", 1));
 */
        model.addAttribute("data", b);

        return "etl_steps";
    }

}
