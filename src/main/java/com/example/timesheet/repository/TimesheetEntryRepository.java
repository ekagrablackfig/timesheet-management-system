package com.example.timesheet.repository;

import com.example.timesheet.entity.TimesheetEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface TimesheetEntryRepository extends JpaRepository<TimesheetEntry, Long> {

    @Query("SELECT e.project.name, SUM(e.hours) FROM TimesheetEntry e GROUP BY e.project.name")
    List<Object[]> findProjectEffortStats();
}
