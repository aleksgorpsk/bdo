package ag.com.dbo.controllers;

import ag.com.dbo.models.management.Node;
import ag.com.dbo.models.management.NodeType;
import ag.com.dbo.services.management.impl.NodeServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
public class NodeController {

    private final NodeServiceImpl nodeService;
    private final List<String> nodeTypes;

    public NodeController(NodeServiceImpl nodeService) {
        this.nodeService = nodeService;
        this.nodeTypes = Arrays.stream(NodeType.values()).map(Enum::name).toList();
    }

    @GetMapping("/node_browser")
    public String getAll(Model model, @RequestParam(required = false) String keyword,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "6") int size,
                         @RequestParam(defaultValue = "id,asc") String[] sort) {
        try {

            String sortField = sort[0];
            String sortDirection = sort[1];

            Direction direction = sortDirection.equals("desc") ? Direction.DESC : Direction.ASC;
            Order order = new Order(direction, sortField);

            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(order));

            Page<@NotNull Node> pageCommon;
            if (keyword == null) {
                pageCommon = nodeService.retrievePage(pageable);
            } else {
                pageCommon = nodeService.findByEtlContainingIgnoreCase(keyword, pageable);
                model.addAttribute("keyword", keyword);
            }

            List<Node> nodeList = pageCommon.getContent();

            model.addAttribute("nodeList", nodeList);
            model.addAttribute("currentPage", pageCommon.getNumber() + 1);
            model.addAttribute("totalItems", pageCommon.getTotalElements());
            model.addAttribute("totalPages", pageCommon.getTotalPages());
            model.addAttribute("pageSize", size);
            model.addAttribute("sortField", sortField);
            model.addAttribute("sortDirection", sortDirection);
            model.addAttribute("reverseSortDirection", sortDirection.equals("asc") ? "desc" : "asc");
        } catch (Exception e) {
            model.addAttribute("message", e.getMessage());
        }

        return "node_browser";
    }

    @GetMapping("/node/new")
    public String addEtl(Model model) {
        Node node = new Node();
        node.setActive(true);

        model.addAttribute("node", node);
        model.addAttribute("allNodeTypes", nodeTypes);
        model.addAttribute("pageTitle", "Create new Node");

        return "node_form";
    }

    @PostMapping("/node/save")
    public String saveEtl(Node node, Model model, RedirectAttributes redirectAttributes) {
        try {

            if (node.getId() != null) {
                nodeService.update(node);
            } else {
                nodeService.create(node);
            }
            redirectAttributes.addFlashAttribute("message", "The Node has been saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addAttribute("message", e.getMessage());
        }

        return "redirect:/node_browser";
    }

    @GetMapping("/node/{id}")
    public String editNode(@PathVariable("id") Integer id,
                          Model model,
                          RedirectAttributes redirectAttributes,
                          @RequestParam Optional<String> message) {
        try {
            Optional<Node> hist = nodeService.findById(id);
            if (hist.isPresent()) {
                Node node = hist.get();
                message.ifPresent(s -> model.addAttribute("message", s));
                model.addAttribute("node", node);
                model.addAttribute("nodeTypes", nodeTypes);
                model.addAttribute("pageTitle", "Edit Node (ID: " + id + ")");
                return "node_form";
            } else {
                redirectAttributes.addFlashAttribute("message", "Etl not found: " + id);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }
        return "redirect:/node_browser";
    }

    @GetMapping("/node/delete/{id}")
    public String deleteEtl(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            nodeService.delete(id);
            redirectAttributes.addFlashAttribute("message", "The Etl with id=" + id + " has been deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }

        return "redirect:/node_browser";
    }

    @GetMapping("/node/{id}/active/{status}")
    public String updateEtlPublishedStatus(@PathVariable("id") Integer id, @PathVariable("status") boolean active,
                                           Model model, RedirectAttributes redirectAttributes) {
        try {
            Optional<Node> hist = nodeService.findById(id);
            if (hist.isPresent()) {
                Node oNode = hist.get();
                oNode.setActive(active);
                nodeService.update(oNode);
            }

            String status = active ? "published" : "disabled";
            String message = "The Etl id=" + id + " has been " + status;
            redirectAttributes.addFlashAttribute("message", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }

        return "redirect:/node_browser";
    }

}
