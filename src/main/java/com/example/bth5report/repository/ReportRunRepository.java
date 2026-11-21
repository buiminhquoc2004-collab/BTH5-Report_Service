package com.example.bth5report.repository;
import com.example.bth5report.model.ReportRun;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ReportRunRepository extends JpaRepository<ReportRun,Long> {
    Optional<ReportRun> findFirstByOrderByIdDesc();
}
