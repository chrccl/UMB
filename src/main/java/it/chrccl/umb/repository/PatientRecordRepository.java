package it.chrccl.umb.repository;

import it.chrccl.umb.model.PatientRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRecordRepository extends JpaRepository<PatientRecord, Long> {
}
