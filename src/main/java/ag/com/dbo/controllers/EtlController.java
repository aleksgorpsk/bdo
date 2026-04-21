package ag.com.dbo.controllers;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import ag.com.dbo.models.EtlDTO;
import ag.com.dbo.services.EngineService;
import ag.com.dbo.services.EtlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
public class EtlController {

//  @Autowired
  private final  EtlService etlService;
    private final EngineService engineService;


    public EtlController(EtlService etlService, EngineService engineService) {
        this.etlService = etlService;
        this.engineService = engineService;
    }

    @GetMapping("/etl_browser")
  public String getAll(Model model, @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "6") int size,
      @RequestParam(defaultValue = "id,asc") String[] sort) {
    try {

      String sortField = sort[0];
      String sortDirection = sort[1];
      
      Direction direction = sortDirection.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
      Order order = new Order(direction, sortField);
      
      Pageable pageable = PageRequest.of(page - 1, size, Sort.by(order));

      Page<EtlDTO> pageTuts;
      if (keyword == null) {
        pageTuts = etlService.retrievePage(pageable);
      } else {
        pageTuts = etlService.findByEtlContainingIgnoreCase(keyword, pageable);
        model.addAttribute("keyword", keyword);
      }

        List<EtlDTO> etlDto = pageTuts.getContent();
      
      model.addAttribute("etlList", etlDto);
      model.addAttribute("currentPage", pageTuts.getNumber() + 1);
      model.addAttribute("totalItems", pageTuts.getTotalElements());
      model.addAttribute("totalPages", pageTuts.getTotalPages());
      model.addAttribute("pageSize", size);
      model.addAttribute("sortField", sortField);
      model.addAttribute("sortDirection", sortDirection);
      model.addAttribute("reverseSortDirection", sortDirection.equals("asc") ? "desc" : "asc");
    } catch (Exception e) {
      model.addAttribute("message", e.getMessage());
    }

    return "etl_browser";
  }

  @GetMapping("/etl/new")
  public String addEtl(Model model) {
    EtlDTO etl = new EtlDTO();
      etl.setActive(true);

    model.addAttribute("etl", etl);
    model.addAttribute("pageTitle", "Create new Etl");

    return "etl_form";
  }

  @PostMapping("/etl/save")
  public String saveEtl(EtlDTO etlDto, RedirectAttributes redirectAttributes) {
    try {
        if (etlDto.getId()!=null) {
            etlService.update(etlDto);
        }else{
            etlService.create(etlDto);
        }
      redirectAttributes.addFlashAttribute("message", "The Etl has been saved successfully!");
    } catch (Exception e) {
      redirectAttributes.addAttribute("message", e.getMessage());
    }

    return "redirect:/etl_browser";
  }

  @GetMapping("/etl/{id}")
  public String editEtl(@PathVariable("id") BigInteger id, Model model, RedirectAttributes redirectAttributes) {
    try {
      Optional<EtlDTO> etl = etlService.findById(id);
      model.addAttribute("etl", etl);
      model.addAttribute("pageTitle", "Edit Etl (ID: " + id + ")");
      return "etl_form";
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("message", e.getMessage());

      return "redirect:/etl_browser";
    }
  }

  @GetMapping("/etl/delete/{id}")
  public String deleteEtl(@PathVariable("id") BigInteger id, Model model, RedirectAttributes redirectAttributes) {
    try {
        etlService.delete(id);

      redirectAttributes.addFlashAttribute("message", "The Etl with id=" + id + " has been deleted successfully!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("message", e.getMessage());
    }

    return "redirect:/etl_browser";
  }

  @GetMapping("/etl/{id}/active/{status}")
  public String updateEtlPublishedStatus(@PathVariable("id") BigInteger id, @PathVariable("status") boolean active,
      Model model, RedirectAttributes redirectAttributes) {
    try {
        Optional<EtlDTO> etl = etlService.findById(id);
        if (etl.isPresent()){
            EtlDTO etlDto= etl.get();
            etlDto.setActive(active);
            etlService.update(etlDto);
        }

      String status = active ? "published" : "disabled";
      String message = "The Etl id=" + id + " has been " + status;
      redirectAttributes.addFlashAttribute("message", message);
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("message", e.getMessage());
    }

    return "redirect:/etl_browser";
  }

    @GetMapping("/etl/start/{id}")
    public String StartEtl(@PathVariable("id") BigInteger id, Model model, RedirectAttributes redirectAttributes) {
        log.info("start!!!! {}", model);
        try {
            Optional<EtlDTO> etldto = etlService.findById(id);
            if (etldto.isPresent()){
                engineService.createEtlInstance(etlService.mapFrom(etldto.get()));
                redirectAttributes.addFlashAttribute("message", "The Etl with id=" + id + " has been deleted successfully!");
            }else{
                redirectAttributes.addFlashAttribute("message", "The Etl with id=" + id + " does not exist!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }

        return "redirect:/etl_browser";
    }

}
