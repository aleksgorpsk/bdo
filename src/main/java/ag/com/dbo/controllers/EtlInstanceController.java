package ag.com.dbo.controllers;

import ag.com.dbo.models.management.EtlDto;
import ag.com.dbo.models.management.EtlInstanceDto;
import ag.com.dbo.services.management.EtlInstanceService;
import ag.com.dbo.services.management.EtlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Optional;


@Slf4j
@RequiredArgsConstructor
@Controller
//@ConditionalOnProperty(name = "dbo.management", havingValue = "true")
public class EtlInstanceController {

    private final EtlInstanceService etlInstanceService;
    private final EtlService etlService;


    @GetMapping(value = {"/etl_instance_browser","/etl_instance_browser/{etlId}"})
    public String getAll(
            @PathVariable(name="etlId", required = false) BigInteger etlId,
            Model model,
//            @ModelAttribute("etlId") String etlId,
            RedirectAttributes redirectAttributes,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {
        try {
            String sortField = sort[0];
            String sortDirection = sort[1];
//            etlId = getEtlId(model.getAttribute("etlId"));

            Direction direction = sortDirection.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Order order = new Order(direction, sortField);

            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(order));

            if (etlId == null) {
                return "redirect:/etl_browser";
            }
            Optional<EtlDto> etlDto = etlService.findById(etlId);
            Page<EtlInstanceDto> etlInstances;
            if (keyword == null) {
                etlInstances = etlInstanceService.retrievePage(etlId, pageable);
            } else {
                etlInstances = etlInstanceService.findByEtlContainingIgnoreCase(etlId, keyword, pageable);
                model.addAttribute("keyword", keyword);
            }

            List<EtlInstanceDto> etlInstanceDto = etlInstances.getContent();

            model.addAttribute("etlInstanceList", etlInstanceDto);
            etlDto.ifPresent(etlDTO -> model.addAttribute("etlDto", etlDTO));
            model.addAttribute("etlId", etlId);
            model.addAttribute("currentPage", etlInstances.getNumber() + 1);
            model.addAttribute("totalItems", etlInstances.getTotalElements());
            model.addAttribute("totalPages", etlInstances.getTotalPages());
            model.addAttribute("pageSize", size);
            model.addAttribute("sortField", sortField);
            model.addAttribute("sortDirection", sortDirection);
            model.addAttribute("reverseSortDirection", sortDirection.equals("asc") ? "desc" : "asc");
            model.addAttribute("pageTitle", "Eit instance");

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
