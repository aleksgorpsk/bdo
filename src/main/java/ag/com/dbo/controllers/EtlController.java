package ag.com.dbo.controllers;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import ag.com.dbo.models.management.Etl;
import ag.com.dbo.models.management.EtlDto;
import ag.com.dbo.models.management.EtlStatus;
import ag.com.dbo.services.management.impl.EngineServiceImpl;
import ag.com.dbo.services.management.impl.EtlServiceImpl;
import ag.com.dbo.services.management.impl.ExternalServiceImpl;
import ag.com.dbo.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
public class EtlController {

    private final EtlServiceImpl etlService;
    private final EngineServiceImpl engineService;
    private final List<String> etlStatuses;
    private final ExternalServiceImpl externalService;

    public EtlController(EtlServiceImpl etlService, EngineServiceImpl engineService, ExternalServiceImpl externalService) {
        this.etlService = etlService;
        this.engineService = engineService;
        this.externalService = externalService;
        this.etlStatuses = Arrays.stream(EtlStatus.values()).map(Enum::name).toList();

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

            Page<EtlDto> pageTuts;
            if (keyword == null) {
                pageTuts = etlService.retrievePage(pageable);
            } else {
                pageTuts = etlService.findByEtlContainingIgnoreCase(keyword, pageable);
                model.addAttribute("keyword", keyword);
            }

            List<EtlDto> etlDto = pageTuts.getContent();

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
    public String addEtl(
            Model model,
            @RequestParam Optional<String> message,
            RedirectAttributes redirectAttributes) {
        EtlDto etl = new EtlDto();
        etl.setActive(true);
        etl.setCronScheduling("");
        message.ifPresent(s -> model.addAttribute("message", s));

        model.addAttribute("etl", etl);
        model.addAttribute("allStatuses", etlStatuses);
        model.addAttribute("pageTitle", "Create new Etl");

        return "etl_form";
    }

    @PostMapping("/etl/save")
    public String saveEtl(EtlDto etl, Model model, RedirectAttributes redirectAttributes) {
        try {
            if (!org.quartz.CronExpression.isValidExpression(etl.getCronScheduling())) {
                redirectAttributes.addAttribute("message", "Incorrect Cron expression: " + etl.getCronScheduling());
                model.addAttribute("EtlDTO", etl);

                if (etl.getId() != null) {
                    return "redirect:/etl/" + etl.getId();
                } else {
                    return "redirect:/etl/new";
                }
            } else {
                redirectAttributes.addAttribute("message", null);
            }
            if (etl.getId() != null) {
                etlService.update(etl);
                externalService.updateSchedule(etlService.mapFrom(etl));
            } else {
                etlService.create(etl);
                externalService.updateSchedule(etlService.mapFrom(etl));
            }
            redirectAttributes.addFlashAttribute("message", "The Etl has been saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addAttribute("message", e.getMessage());
        }

        return "redirect:/etl_browser";
    }

    @GetMapping("/etl/{id}")
    public String editEtl(@PathVariable("id") BigInteger id,
                          Model model,
                          RedirectAttributes redirectAttributes,
                          @RequestParam Optional<String> message) {
        try {
            Optional<EtlDto> oetl = etlService.findById(id);
            if (oetl.isPresent()) {
                EtlDto etl = oetl.get();
                message.ifPresent(s -> model.addAttribute("message", s));
                model.addAttribute("etl", etl);
                model.addAttribute("allStatuses", etlStatuses);
                model.addAttribute("pageTitle", "Edit Etl (ID: " + id + ")");
                return "etl_form";
            } else {
                redirectAttributes.addFlashAttribute("message", "Etl not found: " + id);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }
        return "redirect:/etl_browser";
    }

    @GetMapping("/etl/delete/{id}")
    public String deleteEtl(@PathVariable("id") BigInteger id, Model model, RedirectAttributes redirectAttributes) {
        try {
            etlService.delete(id);
            externalService.deleteSchedule(id);

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
            Optional<EtlDto> etl = etlService.findById(id);
            if (etl.isPresent()) {
                EtlDto etlDto = etl.get();
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
        log.info("start: model {}", model);
        try {
            Optional<EtlDto> etldto = etlService.findById(id);
            if (etldto.isPresent()) {
                engineService.startEtl(etlService.mapFrom(etldto.get()), false);
                redirectAttributes.addFlashAttribute("message", "The Etl with id=" + id + " has been started successfully!");
            } else {
                redirectAttributes.addFlashAttribute("message", "The Etl with id=" + id + " does not exist!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/etl_browser";
    }


    @PutMapping("/schedule")
    public ResponseEntity<String> schedule(@RequestBody ag.com.scheduling.models.ScheduleData request) {
        log.info("schedule:  model{}", request);
        if (request != null) {
            if (request.getEtlId() != null) {
                Optional<Etl> etl = etlService.findEtlById(request.getEtlId());
                if (etl.isPresent()) {
                    engineService.startEtl(etl.get(), true);
                    return ResponseEntity.status(HttpStatus.OK).body(Constants.OK);
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Constants.ERROR);
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Constants.ERROR);
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Constants.ERROR);
        }
    }

}
