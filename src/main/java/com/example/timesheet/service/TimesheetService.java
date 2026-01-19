package com.example.timesheet.service;

import com.example.timesheet.entity.*;
import com.example.timesheet.repository.ProjectRepository;
import com.example.timesheet.repository.TimesheetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
public class TimesheetService {

    @Autowired
    private TimesheetRepository timesheetRepository;

    // userRepo removed

    @Autowired
    private com.example.timesheet.repository.AuditLogRepository auditLogRepository;

    @Autowired
    private ProjectRepository projectRepository;

    public Timesheet getTimesheetById(Long id) {
        return timesheetRepository.findById(id).orElseThrow(() -> new RuntimeException("Timesheet not found"));
    }

    public List<Timesheet> getTimesheetsForUser(User user) {
        return timesheetRepository.findByUserOrderByPeriodStartDateDesc(user);
    }

    public List<Timesheet> getTimesheetsForUser(User user, com.example.timesheet.entity.TimesheetStatus status) {
        if (status == null) {
            return getTimesheetsForUser(user);
        }
        return timesheetRepository.findByUserAndStatusOrderByPeriodStartDateDesc(user, status);
    }

    public List<Timesheet> getAllTimesheets() {
        return timesheetRepository.findAllByOrderByPeriodStartDateDesc();
    }

    public List<Timesheet> getTimesheetsByStatus(TimesheetStatus status) {
        return timesheetRepository.findByStatusOrderByPeriodStartDateDesc(status);
    }

    public List<Timesheet> getPendingTimesheetsForApprover(User approver) {
        return timesheetRepository.findPendingForApproverOrDelegate(approver, LocalDate.now(),
                TimesheetStatus.SUBMITTED);
    }

    @Transactional
    public Timesheet createTimesheet(User user, LocalDate startDate) {
        if (timesheetRepository.existsByUserAndPeriodStartDate(user, startDate)) {
            throw new RuntimeException("A timesheet for this week already exists.");
        }

        Timesheet timesheet = new Timesheet();
        timesheet.setUser(user);
        timesheet.setPeriodStartDate(startDate);
        timesheet.setStatus(TimesheetStatus.DRAFT);
        timesheet.setApprover(user.getManager());
        return timesheetRepository.save(timesheet);
    }

    @Transactional
    public void submitTimesheet(Long timesheetId, User actor) {
        Timesheet timesheet = timesheetRepository.findById(timesheetId)
                .orElseThrow(() -> new RuntimeException("Timesheet not found"));

        // Authorization Check
        if (!timesheet.getUser().getId().equals(actor.getId())) {
            throw new RuntimeException("Not authorized to submit this timesheet");
        }

        if (timesheet.getStatus() != TimesheetStatus.DRAFT && timesheet.getStatus() != TimesheetStatus.REJECTED) {
            throw new RuntimeException("Timesheet can only be submitted from DRAFT or REJECTED status");
        }

        // Validation: Cannot submit empty timesheet
        if (timesheet.getEntries().isEmpty()) {
            throw new RuntimeException("Cannot submit an empty timesheet. Please add entries.");
        }

        // Validation: Cannot submit zero hours
        double totalHours = timesheet.getEntries().stream().mapToDouble(TimesheetEntry::getHours).sum();
        if (totalHours <= 0) {
            throw new RuntimeException("Total hours must be greater than zero.");
        }

        timesheet.setStatus(TimesheetStatus.SUBMITTED);
        timesheet.setSubmissionDate(java.time.LocalDateTime.now());
        timesheet.setRejectionReason(null); // Clear previous rejection reason

        if (timesheet.getApprover() == null) {
            timesheet.setApprover(timesheet.getUser().getManager());
        }
        timesheetRepository.save(timesheet);

        // Audit Log
        auditLogRepository.save(new AuditLog("SUBMIT", timesheet.getUser().getUsername(), timesheetId,
                "Timesheet submitted for approval"));
    }

    @Transactional
    public void approveTimesheet(Long timesheetId, User approver) {
        Timesheet timesheet = timesheetRepository.findById(timesheetId)
                .orElseThrow(() -> new RuntimeException("Timesheet not found"));

        if (timesheet.getStatus() != TimesheetStatus.SUBMITTED) {
            throw new RuntimeException("Timesheet must be in SUBMITTED status to be approved");
        }

        // Validate Approver: Is Assigned Approver OR Is Valid Delegate
        boolean isAssigned = timesheet.getApprover() != null
                && timesheet.getApprover().getId().equals(approver.getId());
        boolean isDelegate = false;

        if (!isAssigned && timesheet.getApprover() != null && timesheet.getApprover().getDelegate() != null) {
            if (timesheet.getApprover().getDelegate().getId().equals(approver.getId())) {
                LocalDate expiry = timesheet.getApprover().getDelegationExpiry();
                if (expiry == null || !expiry.isBefore(LocalDate.now())) {
                    isDelegate = true;
                }
            }
        }

        if (!isAssigned && !isDelegate && !approver.getRole().name().equals("ADMIN")) {
            throw new RuntimeException("Not authorized to approve this timesheet");
        }

        timesheet.setStatus(TimesheetStatus.APPROVED);
        timesheet.setApprovalDate(java.time.LocalDateTime.now());
        timesheet.setActualApprover(approver);

        timesheetRepository.save(timesheet);

        auditLogRepository.save(new AuditLog("APPROVE", approver.getUsername(), timesheetId,
                "Timesheet approved by " + approver.getUsername()));
    }

    @Transactional
    public void rejectTimesheet(Long timesheetId, User approver, String reason) {
        Timesheet timesheet = timesheetRepository.findById(timesheetId)
                .orElseThrow(() -> new RuntimeException("Timesheet not found"));

        if (timesheet.getStatus() != TimesheetStatus.SUBMITTED) {
            throw new RuntimeException("Timesheet must be in SUBMITTED status to be rejected");
        }

        // Validate Approver logic same as approve (delegates can reject too)
        boolean isAssigned = timesheet.getApprover() != null
                && timesheet.getApprover().getId().equals(approver.getId());
        boolean isDelegate = false;
        if (!isAssigned && timesheet.getApprover() != null && timesheet.getApprover().getDelegate() != null) {
            if (timesheet.getApprover().getDelegate().getId().equals(approver.getId())) {
                LocalDate expiry = timesheet.getApprover().getDelegationExpiry();
                if (expiry == null || !expiry.isBefore(LocalDate.now())) {
                    isDelegate = true;
                }
            }
        }

        if (!isAssigned && !isDelegate && !approver.getRole().name().equals("ADMIN")) {
            throw new RuntimeException("Not authorized to reject this timesheet");
        }

        timesheet.setStatus(TimesheetStatus.REJECTED);
        timesheet.setRejectionReason(reason);
        timesheet.setActualApprover(approver); // Captured who rejected it

        timesheetRepository.save(timesheet);

        auditLogRepository
                .save(new AuditLog("REJECT", approver.getUsername(), timesheetId, "Timesheet rejected: " + reason));
    }

    @Transactional
    public void addEntry(Long timesheetId, Long projectId, LocalDate date, Double hours, String attributes,
            User actor) {
        Timesheet timesheet = timesheetRepository.findById(timesheetId)
                .orElseThrow(() -> new RuntimeException("Timesheet not found"));

        // Authorization Check
        if (!timesheet.getUser().getId().equals(actor.getId())) {
            throw new RuntimeException("Not authorized to edit this timesheet");
        }

        if (timesheet.getStatus() != TimesheetStatus.DRAFT && timesheet.getStatus() != TimesheetStatus.REJECTED) {
            throw new RuntimeException("Cannot add entries to a non-draft timesheet");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        TimesheetEntry entry = new TimesheetEntry();
        entry.setTimesheet(timesheet);
        entry.setProject(project);
        entry.setDate(date);
        entry.setHours(hours);
        entry.setAttributes(attributes);

        timesheet.getEntries().add(entry);
        timesheetRepository.save(timesheet);
    }

    @Transactional
    public void deleteTimesheet(Long timesheetId, User actor) {
        Timesheet timesheet = timesheetRepository.findById(timesheetId)
                .orElseThrow(() -> new RuntimeException("Timesheet not found"));

        if (!timesheet.getUser().getId().equals(actor.getId())) {
            throw new RuntimeException("Not authorized to delete this timesheet");
        }

        if (timesheet.getStatus() != TimesheetStatus.DRAFT) {
            throw new RuntimeException("Can only delete timesheets in DRAFT status");
        }

        timesheetRepository.delete(timesheet);
    }

    @org.springframework.transaction.annotation.Transactional
    public void saveTimesheetGrid(Long timesheetId, User user, com.example.timesheet.dto.TimesheetGridDTO gridDTO) {
        Timesheet timesheet = getTimesheetById(timesheetId);

        // Security Check: Ownership
        if (!timesheet.getUser().getId().equals(user.getId()) && !user.getRole().name().equals("ADMIN")) {
            throw new RuntimeException("security.access_denied");
        }

        // State Check: Draft or Rejected
        if (timesheet.getStatus() != TimesheetStatus.DRAFT && timesheet.getStatus() != TimesheetStatus.REJECTED) {
            throw new RuntimeException("Cannot edit a timesheet that is not in DRAFT or REJECTED state");
        }

        // 1. Clear existing entries (Full wipe to match grid state)
        timesheet.getEntries().clear();

        // 2. Rebuild entries from Grid Rows
        if (gridDTO.getRows() != null) {
            for (com.example.timesheet.dto.TimesheetGridDTO.TimesheetRowDTO row : gridDTO.getRows()) {
                if (row.getProjectId() == null)
                    continue; // Skip empty rows

                com.example.timesheet.entity.Project project = projectRepository.findById(row.getProjectId())
                        .orElseThrow();

                // Pack attributes
                java.util.Map<String, String> attrs = new java.util.HashMap<>();
                if (row.getCategory() != null)
                    attrs.put("Category", row.getCategory());
                if (row.getTaskKey() != null)
                    attrs.put("TaskKey", row.getTaskKey());
                if (row.getTaskDetails() != null)
                    attrs.put("TaskDetails", row.getTaskDetails());

                String attrJson = "{}";
                if (!attrs.isEmpty()) {
                    try {
                        StringBuilder sb = new StringBuilder("{");
                        for (java.util.Map.Entry<String, String> e : attrs.entrySet()) {
                            if (sb.length() > 1)
                                sb.append(",");
                            sb.append("\"").append(e.getKey()).append("\":\"")
                                    .append(e.getValue().replace("\"", "\\\"")).append("\"");
                        }
                        sb.append("}");
                        attrJson = sb.toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // Create entries for each non-zero day
                LocalDate start = timesheet.getPeriodStartDate();
                createEntryIfValid(timesheet, project, start, row.getMon(), attrJson); // Mon
                createEntryIfValid(timesheet, project, start.plusDays(1), row.getTue(), attrJson); // Tue
                createEntryIfValid(timesheet, project, start.plusDays(2), row.getWed(), attrJson); // Wed
                createEntryIfValid(timesheet, project, start.plusDays(3), row.getThu(), attrJson); // Thu
                createEntryIfValid(timesheet, project, start.plusDays(4), row.getFri(), attrJson); // Fri
                createEntryIfValid(timesheet, project, start.plusDays(5), row.getSat(), attrJson); // Sat
                createEntryIfValid(timesheet, project, start.plusDays(6), row.getSun(), attrJson); // Sun
            }
        }

        timesheetRepository.save(timesheet);
    }

    private void createEntryIfValid(Timesheet t, com.example.timesheet.entity.Project p, LocalDate d, Double hours,
            String attr) {
        if (hours != null && hours > 0) {
            TimesheetEntry entry = new TimesheetEntry();
            entry.setTimesheet(t);
            entry.setProject(p);
            entry.setDate(d);
            entry.setHours(hours);
            entry.setAttributes(attr);
            t.getEntries().add(entry);
        }
    }
}
