package it.chrccl.umb.repository;

import it.chrccl.umb.model.PatientRecord;
import it.chrccl.umb.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PatientRecordRepository extends JpaRepository<PatientRecord, Long> {

    /**
     * Find patient record by patient's mobile phone
     */
    @Query("SELECT pr FROM PatientRecord pr WHERE pr.patient.mobilePhone = :mobilePhone")
    Optional<PatientRecord> findByPatientMobilePhone(@Param("mobilePhone") String mobilePhone);

    /**
     * Find patient record by patient entity
     */
    Optional<PatientRecord> findByPatient(User patient);
}