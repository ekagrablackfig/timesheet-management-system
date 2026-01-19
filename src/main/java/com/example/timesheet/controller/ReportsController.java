package com.example.timesheet.controller;

import com.example.timesheet.repository.TimesheetEntryRepository;
import com.example.timesheet.repository.TimesheetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/reports")
@PreAuthorize("hasRole('ADMIN')")
public class ReportsController {

    @Autowired
    private TimesheetEntryRepository timesheetEntryRepository;

    @Autowired
    private TimesheetRepository timesheetRepository;

    @GetMapping
    public String index(Model model) {
        // Project Effort Stats
        List<Object[]> projectStats = timesheetEntryRepository.findProjectEffortStats();
        model.addAttribute("projectStats", projectStats);

        // Fetch all timesheets
        List<com.example.timesheet.entity.Timesheet> allTimesheets = timesheetRepository.findAll();
        model.addAttribute("allTimesheets", allTimesheets);

        // Compute Status Counts
        java.util.Map<com.example.timesheet.entity.TimesheetStatus, Long> statusCounts = new java.util.HashMap<>();
        for (com.example.timesheet.entity.TimesheetStatus status : com.example.timesheet.entity.TimesheetStatus
                .values()) {
            statusCounts.put(status, allTimesheets.stream().filter(t -> t.getStatus() == status).count());
        }
        model.addAttribute("statusCounts", statusCounts);

        return "reports/index";
    }
}
