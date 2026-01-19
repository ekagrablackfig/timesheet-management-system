package com.example.timesheet.repository;

import com.example.timesheet.entity.Timesheet;
import com.example.timesheet.entity.TimesheetStatus;
import com.example.timesheet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface TimesheetRepository extends JpaRepository<Timesheet, Long> {
    // Filtered by status
    List<Timesheet> findByUserAndStatusOrderByPeriodStartDateDesc(User user, TimesheetStatus status);

    List<Timesheet> findByStatusOrderByPeriodStartDateDesc(TimesheetStatus status);

    List<Timesheet> findByUserOrderByPeriodStartDateDesc(User user);

    // Default findAll but sorted
    List<Timesheet> findAllByOrderByPeriodStartDateDesc();

    // Kept for backward compat but usage should move to Sorted versions
    List<Timesheet> findByUser(User user);

    List<Timesheet> findByApproverAndStatus(User approver, TimesheetStatus status);

    @Query("SELECT t FROM Timesheet t WHERE t.status = :status AND (t.approver = :approver OR (t.approver.delegate = :approver AND t.approver.delegationExpiry >= :today))")
    List<Timesheet> findPendingForApproverOrDelegate(@Param("approver") User approver,
            @Param("today") LocalDate today,
            @Param("status") TimesheetStatus status);

    boolean existsByUserAndPeriodStartDate(User user, LocalDate periodStartDate);

    // Reporting: Find all timesheet entries with project details
    // Ideally we would aggregate here, but for simplicity let's fetch entries and
    // aggregate in memory or controller
    // unless we need strictly DB aggregation. DB aggregation is better for "Project
    // Effort"
    // We need: Project Name, Total Hours
    // JPQL: SELECT e.project.name, SUM(e.hours) FROM TimesheetEntry e GROUP BY
    // e.project.name
    // But TimesheetEntry is not directly a repository here. Let's create
    // TimesheetEntryRepository for this.
}
