package ag.com.dbo.controllers;

import ag.com.dbo.models.management.StepInstanceDTO;
import ag.com.dbo.services.management.StepInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigInteger;
import java.util.List;


@Slf4j
@Controller
@ConditionalOnProperty(name = "dbo.management", havingValue = "true")
public class StepInstanceController {

    private final StepInstanceService stepInstanceService;


    public StepInstanceController(StepInstanceService stepInstanceService) {
        this.stepInstanceService = stepInstanceService;
    }

    @GetMapping("/step_instance_browser/{etlInstanceId}")
    public String getInstanceAll(
            @PathVariable("etlInstanceId") BigInteger etlId,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        redirectAttributes.addFlashAttribute("etlInstanceId", etlId);
        return "redirect:/step_instance_browser";
    }


    @GetMapping("/step_instance_browser")
    public String getAll(
            Model model,
            @ModelAttribute("etlInstanceId") String etlInstanceId2,
            RedirectAttributes redirectAttributes,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {
        try {

            String sortField = sort[0];
            String sortDirection = sort[1];
            BigInteger etlInstanceId = getEtlId(model.getAttribute("etlInstanceId"));

            Direction direction = sortDirection.equals("desc") ? Direction.DESC : Direction.ASC;
            Order order = new Order(direction, sortField);

            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(order));

            if (etlInstanceId == null) {
                return "redirect:/etl_browser";
            }
            Page<StepInstanceDTO> stepInstances;
            if (keyword == null) {
                stepInstances = stepInstanceService.retrievePage(etlInstanceId, pageable);
            } else {
                stepInstances = stepInstanceService.findByEtlContainingIgnoreCase(etlInstanceId, keyword, pageable);
                model.addAttribute("keyword", keyword);
            }

            List<StepInstanceDTO> stepInstanceList = stepInstances.getContent();

            model.addAttribute("stepInstanceList", stepInstanceList);
            model.addAttribute("etlInstanceId", etlInstanceId);
            model.addAttribute("currentPage", stepInstances.getNumber() + 1);
            model.addAttribute("totalItems", stepInstances.getTotalElements());
            model.addAttribute("totalPages", stepInstances.getTotalPages());
            model.addAttribute("pageSize", size);
            model.addAttribute("sortField", sortField);
            model.addAttribute("sortDirection", sortDirection);
            model.addAttribute("reverseSortDirection", sortDirection.equals("asc") ? "desc" : "asc");
            model.addAttribute("pageTitle", "Eit instance");

//            redirectAttributes.addFlashAttribute("etlId", etlId);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", e.getMessage());
        }

        return "step_instance_browser";
    }

    private BigInteger getEtlId(Object data) {
        BigInteger etlId = null;
        if (data != null) {
            if (data instanceof BigInteger) {
                etlId = (BigInteger) data;
            } else if (data instanceof String) {
                try {
                    etlId = new BigInteger((String) data);
                } catch (NumberFormatException ignored) {; }
            }
        }
        return etlId;
    }
}
