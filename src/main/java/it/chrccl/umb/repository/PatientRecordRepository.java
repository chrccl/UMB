package it.chrccl.umb.repository;

import it.chrccl.umb.model.Issue;
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

    /**
     * AGGIUNTO: Find patient record by patient and issue type
     * This prevents duplicates for the same patient with the same issue
     */
    Optional<PatientRecord> findByPatientAndIssue(User patient, Issue issue);

    /**
     * AGGIUNTO: Find patient record by mobile phone and issue type
     */
    @Query("SELECT pr FROM PatientRecord pr WHERE pr.patient.mobilePhone = :mobilePhone AND pr.issue = :issue")
    Optional<PatientRecord> findByPatientMobilePhoneAndIssue(@Param("mobilePhone") String mobilePhone, @Param("issue") Issue issue);
}