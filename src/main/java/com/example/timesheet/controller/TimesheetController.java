package com.example.timesheet.controller;

import com.example.timesheet.entity.Timesheet;
import com.example.timesheet.entity.User;
import com.example.timesheet.service.ProjectService;
import com.example.timesheet.service.TimesheetService;

import com.example.timesheet.repository.UserRepository; // Direct access useful for finding current user
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/timesheets")
public class TimesheetController {

    @Autowired
    private TimesheetService timesheetService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String listTimesheets(Model model,
            @RequestParam(required = false) com.example.timesheet.entity.TimesheetStatus status,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        if (user.getRole().name().equals("APPROVER")) {
            List<Timesheet> pending = timesheetService.getPendingTimesheetsForApprover(user);
            model.addAttribute("pendingTimesheets", pending);
            // Add users for delegation dropdown (anyone can delegate, but usually
            // approvers)
            model.addAttribute("users", userRepository.findAll()); // Simple list suitable for small org
        }

        // Feature: Admin Visibility - Admins see ALL timesheets
        List<Timesheet> timesheets;
        if (user.getRole().name().equals("ADMIN")) {
            if (status != null) {
                timesheets = timesheetService.getTimesheetsByStatus(status);
            } else {
                timesheets = timesheetService.getAllTimesheets();
            }
        } else {
            // Apply filtering for standard users
            timesheets = timesheetService.getTimesheetsForUser(user, status);
        }

        model.addAttribute("timesheets", timesheets);
        model.addAttribute("currentStatus", status); // For highlighting active filter
        return "timesheet/list";
    }

    @PostMapping("/delegate")
    public String setDelegate(@RequestParam(required = false) Long delegateId,
            @RequestParam(required = false) LocalDate expiryDate,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        if (delegateId != null) {
            User delegate = userRepository.findById(delegateId).orElseThrow();
            user.setDelegate(delegate);
            user.setDelegationExpiry(expiryDate);
        } else {
            user.setDelegate(null);
            user.setDelegationExpiry(null);
        }
        userRepository.save(user);
        return "redirect:/timesheets";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        // Generate list of weeks (e.g., Current week, Next 2 weeks, Past 4 weeks)
        List<WeekOption> weekOptions = new java.util.ArrayList<>();
        LocalDate today = LocalDate.now();
        // Find current Monday
        LocalDate currentMonday = today
                .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

        // Generate options: 2 weeks future, current, 8 weeks past
        for (int i = 2; i >= -8; i--) {
            LocalDate monday = currentMonday.plusWeeks(i);
            LocalDate sunday = monday.plusDays(6);

            // ISO Week Number
            int weekNum = monday.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            int year = monday.get(java.time.temporal.IsoFields.WEEK_BASED_YEAR);

            String label = String.format("Year %d Week %d (%s - %s)",
                    year,
                    weekNum,
                    monday.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    sunday.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")));

            if (i == 0)
                label = "Current Week " + label;

            WeekOption opt = new WeekOption(monday, label);
            weekOptions.add(opt);
        }

        // Sort Ascending (Past -> Future)
        weekOptions.sort((a, b) -> a.startDate.compareTo(b.startDate));

        model.addAttribute("weekOptions", weekOptions);
        return "timesheet/create";
    }

    // Helper DTO class
    public static class WeekOption {
        private LocalDate startDate;
        private String label;

        public WeekOption(LocalDate s, String l) {
            this.startDate = s;
            this.label = l;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public String getLabel() {
            return label;
        }
    }

    @PostMapping("/create")
    public String create(@RequestParam LocalDate startDate, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        // Adjust to Monday just in case
        LocalDate monday = startDate
                .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        timesheetService.createTimesheet(user, monday);
        return "redirect:/timesheets";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        Timesheet timesheet = timesheetService.getTimesheetById(id); // Need to implement getTimesheetById
        model.addAttribute("timesheet", timesheet);
        model.addAttribute("projects", projectService.getAllProjects());
        return "timesheet/view";
    }

    @PostMapping("/{id}/entry")
    public String addEntry(@PathVariable Long id, @RequestParam Long projectId, @RequestParam LocalDate date,
            @RequestParam Double hours, @RequestParam(required = false) String attributes,
            @RequestParam java.util.Map<String, String> allParams,
            @AuthenticationPrincipal UserDetails userDetails) {

        String attributesJson = attributes;

        // Fallback: If attributes JSON is not provided directly, try to build it from
        // loose params (Legacy support)
        if (attributesJson == null || attributesJson.trim().isEmpty()) {
            java.util.Map<String, String> attrMap = new java.util.HashMap<>();
            for (java.util.Map.Entry<String, String> entry : allParams.entrySet()) {
                if (!entry.getKey().equals("projectId") && !entry.getKey().equals("date")
                        && !entry.getKey().equals("hours")
                        && !entry.getKey().equals("_csrf") && !entry.getKey().equals("attributes")) {
                    attrMap.put(entry.getKey(), entry.getValue());
                }
            }
            if (!attrMap.isEmpty()) {
                StringBuilder sb = new StringBuilder("{");
                for (java.util.Map.Entry<String, String> e : attrMap.entrySet()) {
                    if (sb.length() > 1)
                        sb.append(",");
                    sb.append("\"").append(e.getKey()).append("\":\"").append(e.getValue()).append("\"");
                }
                sb.append("}");
                attributesJson = sb.toString();
            }
        }

        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        timesheetService.addEntry(id, projectId, date, hours, attributesJson, user);
        return "redirect:/timesheets/" + id;
    }

    @PostMapping("/{id}/submit")
    public String submit(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        timesheetService.submitTimesheet(id, user);
        return "redirect:/timesheets";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User approver = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        timesheetService.approveTimesheet(id, approver);
        return "redirect:/timesheets";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, @RequestParam(required = false) String reason,
            @AuthenticationPrincipal UserDetails userDetails) {
        User approver = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        String rejectionReason = (reason == null || reason.trim().isEmpty()) ? "No reason provided" : reason;
        timesheetService.rejectTimesheet(id, approver, rejectionReason);
        return "redirect:/timesheets";
    }

    @PostMapping("/{id}/grid")
    public String updateGrid(@PathVariable Long id, @ModelAttribute com.example.timesheet.dto.TimesheetGridDTO gridDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        timesheetService.saveTimesheetGrid(id, user, gridDTO);
        return "redirect:/timesheets/" + id;
    }

    @GetMapping("/export")
    public void exportToCSV(jakarta.servlet.http.HttpServletResponse response,
            @AuthenticationPrincipal UserDetails userDetails) throws java.io.IOException {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        response.setContentType("text/csv");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=timesheets_" + LocalDate.now() + ".csv";
        response.setHeader(headerKey, headerValue);

        java.util.List<Timesheet> timesheets;
        if (user.getRole().name().equals("ADMIN")) {
            timesheets = timesheetService.getAllTimesheets();
        } else {
            // For simplicity, export user's own timesheets.
            // Ideally could export approved ones for managers too.
            timesheets = timesheetService.getTimesheetsForUser(user);
        }

        java.io.Writer writer = response.getWriter();
        writer.write("ID,Period Start,Status,Approver,User\n");
        for (Timesheet t : timesheets) {
            writer.write(t.getId() + "," + t.getPeriodStartDate() + "," + t.getStatus() + "," +
                    (t.getApprover() != null ? t.getApprover().getUsername() : "") + "," +
                    t.getUser().getUsername() + "\n");
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteTimesheet(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        timesheetService.deleteTimesheet(id, user);
        return "redirect:/timesheets";
    }
}
