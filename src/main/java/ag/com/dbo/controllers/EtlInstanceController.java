package ag.com.dbo.controllers;

import ag.com.dbo.models.management.EtlInstanceDTO;
import ag.com.dbo.services.management.EtlInstanceService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigInteger;
import java.util.List;


@Slf4j
@Controller
@ConditionalOnProperty(name = "dbo.management", havingValue = "true")
public class EtlInstanceController {

    private final EtlInstanceService etlInstanceService;


    public EtlInstanceController(EtlInstanceService etlInstanceService
    ) {
        this.etlInstanceService = etlInstanceService;
    }

    @GetMapping("/etl_instance_browser/{etlId}")
    public String getInstanceAll(
            @PathVariable("etlId") BigInteger etlId,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        redirectAttributes.addFlashAttribute("etlId", etlId);
        return "redirect:/etl_instance_browser";
    }


    @GetMapping("/etl_instance_browser")
    public String getAll(
            Model model,
            @ModelAttribute("etlId") String etlId2,
//            RedirectAttributes redirectAttributes,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {
        try {

            String sortField = sort[0];
            String sortDirection = sort[1];
            BigInteger etlId = getEtlId(model.getAttribute("etlId"));

            Direction direction = sortDirection.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Order order = new Order(direction, sortField);

            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(order));

            if (etlId == null) {
                return "redirect:/etl_browser";
            }
            Page<EtlInstanceDTO> etlInstances;
            if (keyword == null) {
                etlInstances = etlInstanceService.retrievePage(etlId, pageable);
            } else {
                etlInstances = etlInstanceService.findByEtlContainingIgnoreCase(etlId, keyword, pageable);
                model.addAttribute("keyword", keyword);
            }

            List<EtlInstanceDTO> etlDto = etlInstances.getContent();

            model.addAttribute("etlInstanceList", etlDto);
            model.addAttribute("etlId", etlId);
            model.addAttribute("currentPage", etlInstances.getNumber() + 1);
            model.addAttribute("totalItems", etlInstances.getTotalElements());
            model.addAttribute("totalPages", etlInstances.getTotalPages());
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

        return "etl_instance_browser";
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
