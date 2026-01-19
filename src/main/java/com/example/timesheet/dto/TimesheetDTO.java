package com.example.timesheet.dto;

import com.example.timesheet.entity.TimesheetStatus;
import java.time.LocalDate;
import java.util.List;

public class TimesheetDTO {
    private Long id;
    private Long userId;
    private String userName;
    private LocalDate periodStartDate;
    private TimesheetStatus status;
    private List<TimesheetEntryDTO> entries;

    public TimesheetDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LocalDate getPeriodStartDate() {
        return periodStartDate;
    }

    public void setPeriodStartDate(LocalDate periodStartDate) {
        this.periodStartDate = periodStartDate;
    }

    public TimesheetStatus getStatus() {
        return status;
    }

    public void setStatus(TimesheetStatus status) {
        this.status = status;
    }

    public List<TimesheetEntryDTO> getEntries() {
        return entries;
    }

    public void setEntries(List<TimesheetEntryDTO> entries) {
        this.entries = entries;
    }
}
