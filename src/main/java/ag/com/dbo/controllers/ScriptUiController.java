package ag.com.dbo.controllers;

import ag.com.dbo.models.script.*;
import ag.com.dbo.services.management.ScriptService;
import ag.com.dbo.services.script.GroovyService;
import ag.com.dbo.services.script.PythonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
//@RequestMapping("/queue")
@Slf4j
public class ScriptUiController {

    private final ScriptService scriptService;
    private final GroovyService groovyService;
    private final PythonService pythonService;

    private final List<String> allLanguages;
    private final List<String> allTypes;



    public ScriptUiController(ScriptService scriptService, GroovyService groovyService, PythonService pythonService) {
        this.scriptService = scriptService;
        this.groovyService = groovyService;
        this.pythonService = pythonService;
        this.allLanguages = Arrays.stream(ScriptLanguage.values()).map(Enum::name).toList();
        this.allTypes = Arrays.stream(ScriptType.values()).map(Enum::name).toList();

    }

/// -------- script UI controller


@GetMapping("/script_browser")
public String getAll(Model model, @RequestParam(required = false) String keyword,
                     @RequestParam(defaultValue = "1") int page,
                     @RequestParam(defaultValue = "6") int size,
                     @RequestParam(defaultValue = "id,asc") String[] sort) {
    try {

        String sortField = sort[0];
        String sortDirection = sort[1];

        Sort.Direction direction = sortDirection.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort.Order order = new Sort.Order(direction, sortField);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(order));

        Page<ScriptDto> scriptPage;
        if (keyword == null) {
            scriptPage = scriptService.retrievePage(pageable);
        } else {
            scriptPage = scriptService.findByScriptContainingIgnoreCase(keyword, pageable);
            model.addAttribute("keyword", keyword);
        }

        List<ScriptDto> scriptDtoList = scriptPage.getContent();

        model.addAttribute("scriptList", scriptDtoList);
        model.addAttribute("currentPage", scriptPage.getNumber() + 1);
        model.addAttribute("totalItems", scriptPage.getTotalElements());
        model.addAttribute("totalPages", scriptPage.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("reverseSortDirection", sortDirection.equals("asc") ? "desc" : "asc");
    } catch (Exception e) {
        model.addAttribute("message", e.getMessage());
    }

    return "script_browser";
}

    @GetMapping("/script/new")
    public String addScript(Model model,
                         @RequestParam Optional<String> message,
                         RedirectAttributes redirectAttributes) {
        ScriptDto script = new ScriptDto();
        script.setActive(true);
        message.ifPresent(s -> model.addAttribute("message", s));
        model.addAttribute("script", script);

        model.addAttribute("script", script);
        model.addAttribute("allLanguages", allLanguages);
        model.addAttribute("allTypes", allTypes);

        model.addAttribute("pageTitle", "Create new Script");

        return "script_form";
    }

    @PostMapping("/script/save")
    public String saveScript(ScriptDto script, Model model, RedirectAttributes redirectAttributes) {
        Script scr = scriptService.getScript(script);
        try {
             scriptService.update(script);
            redirectAttributes.addFlashAttribute("message", "The Script has been saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addAttribute("message", e.getMessage());
        }

        return "redirect:/script_browser";
    }


    /*
    Update
     */
//    @GetMapping("/etl/{id}")

    @GetMapping("/script/update/{id}")
    public String editScript( @PathVariable BigInteger id,
                          Model model,
                          RedirectAttributes redirectAttributes,
                          @RequestParam Optional<String> message) {
        try {
            Optional<ScriptDto> oscript = scriptService.findById(id);
            if (oscript.isPresent()) {
                ScriptDto script = oscript.get();
                message.ifPresent(s -> model.addAttribute("message", s));
                model.addAttribute("script", script);
                model.addAttribute("pageTitle", "Edit Script (ID: " + id + ")");
                return "script_form";
            } else {
                redirectAttributes.addFlashAttribute("message", "Script not found: " + id);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }
        return "redirect:/script_browser";
    }

    @GetMapping("/script/delete/{id}")
    public String deleteScript(
            @PathVariable BigInteger id,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            scriptService.delete(id);

            redirectAttributes.addFlashAttribute("message", "The Script with id=" + id + " has been deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }

        return "redirect:/script_browser";
    }

    @GetMapping("/script/{id}/active/{status}")
    public String updateScriptPublishedStatus(
            @PathVariable BigInteger id,
            @PathVariable("status") boolean active,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {

            Optional<ScriptDto> script = scriptService.findById(id);
            if (script.isPresent()) {
                ScriptDto scriptDto = script.get();
                scriptDto.setActive(active);
                scriptService.update(scriptDto);
            }

            String status = active ? "published" : "disabled";
            String message = "The Script id=" + id + " has been " + status;
            redirectAttributes.addFlashAttribute("message", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }

        return "redirect:/script_browser";
    }

}
