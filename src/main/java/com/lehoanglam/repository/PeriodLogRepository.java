package com.yes4all.repository;

import com.yes4all.domain.PeriodLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the PeriodLog entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PeriodLogRepository extends JpaRepository<PeriodLog, Long> {
    PeriodLog findByDayAndMonthAndYear(int day, int month, int year);
}
