package com.example.timesheet.controller;

import com.example.timesheet.entity.Project;
import com.example.timesheet.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private com.example.timesheet.service.UserService userService;

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    @GetMapping("/users/create")
    public String createUserForm(Model model) {
        model.addAttribute("user", new com.example.timesheet.entity.User());
        model.addAttribute("roles", com.example.timesheet.entity.Role.values());
        model.addAttribute("users", userService.getAllUsers()); // For Manager selection
        return "admin/create_user";
    }

    @PostMapping("/users/create")
    public String createUser(@ModelAttribute com.example.timesheet.entity.User user) {
        userService.saveUser(user);
        return "redirect:/admin/users";
    }

    @GetMapping("/projects")
    public String listProjects(Model model) {
        model.addAttribute("projects", projectService.getAllProjects());
        return "admin/projects";
    }

    @GetMapping("/projects/create")
    public String createProjectForm(Model model) {
        model.addAttribute("project", new Project());
        return "admin/create_project";
    }

    @PostMapping("/projects/create")
    public String createProject(@ModelAttribute Project project) {
        projectService.saveProject(project);
        return "redirect:/admin/projects";
    }

    @GetMapping("/users/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        model.addAttribute("roles", com.example.timesheet.entity.Role.values());
        model.addAttribute("users", userService.getAllUsers());
        return "admin/edit_user";
    }

    @PostMapping("/users/{id}/edit")
    public String editUser(@PathVariable Long id, @ModelAttribute com.example.timesheet.entity.User user) {
        user.setId(id); // Ensure ID is preserved
        userService.saveUser(user);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Cannot delete user. They likely have associated timesheets or are managing other users.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/projects/{id}/delete")
    public String deleteProject(@PathVariable Long id,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            projectService.deleteProject(id);
            redirectAttributes.addFlashAttribute("successMessage", "Project deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Cannot delete project. It likely has associated timesheets.");
        }
        return "redirect:/admin/projects";
    }

    @GetMapping("/projects/{id}/edit")
    public String editProjectForm(@PathVariable Long id, Model model) {
        model.addAttribute("project", projectService.getProjectById(id));
        return "admin/edit_project";
    }

    @PostMapping("/projects/{id}/edit")
    public String editProject(@PathVariable Long id, @ModelAttribute Project project) {
        project.setId(id);
        projectService.saveProject(project);
        return "redirect:/admin/projects";
    }
}
