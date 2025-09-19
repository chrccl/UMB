package it.chrccl.umb.controller;

import it.chrccl.umb.model.Conversation;
import it.chrccl.umb.model.PatientRecord;
import it.chrccl.umb.repository.ConversationRepository;
import it.chrccl.umb.repository.PatientRecordRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // For development - configure properly for production
public class DashboardController {

    private final ConversationRepository conversationRepository;
    private final PatientRecordRepository patientRecordRepository;

    public DashboardController(ConversationRepository conversationRepository,
                               PatientRecordRepository patientRecordRepository) {
        this.conversationRepository = conversationRepository;
        this.patientRecordRepository = patientRecordRepository;
    }

    /**
     * Get all conversations with their messages
     */
    @GetMapping("/conversations")
    public List<Conversation> getAllConversations() {
        return conversationRepository.findAll();
    }

    /**
     * Get a specific conversation by ID
     */
    @GetMapping("/conversations/{id}")
    public ResponseEntity<Conversation> getConversation(@PathVariable Long id) {
        Optional<Conversation> conversation = conversationRepository.findById(id);
        return conversation.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Mark a conversation as contacted
     */
    @PatchMapping("/conversations/{id}/mark-contacted")
    public ResponseEntity<Conversation> markAsContacted(@PathVariable Long id) {
        try {
            Optional<Conversation> conversationOpt = conversationRepository.findById(id);

            if (conversationOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Conversation conversation = conversationOpt.get();

            // Find or create a PatientRecord for this conversation and mark it as contacted
            List<PatientRecord> records = patientRecordRepository.findAll().stream()
                    .filter(pr -> pr.getPatient() != null &&
                            pr.getPatient().getMobilePhone().equals(conversation.getUser().getMobilePhone()))
                    .toList();

            if (!records.isEmpty()) {
                PatientRecord record = records.get(0);
                record.markAsContacted(); // Using the new method from enhanced entity
                patientRecordRepository.save(record);
            }

            return ResponseEntity.ok(conversation);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Mark a conversation as unread
     */
    @PatchMapping("/conversations/{id}/mark-unread")
    public ResponseEntity<Conversation> markAsUnread(@PathVariable Long id) {
        try {
            Optional<Conversation> conversationOpt = conversationRepository.findById(id);

            if (conversationOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Conversation conversation = conversationOpt.get();

            // Remove contacted status from PatientRecord
            List<PatientRecord> records = patientRecordRepository.findAll().stream()
                    .filter(pr -> pr.getPatient() != null &&
                            pr.getPatient().getMobilePhone().equals(conversation.getUser().getMobilePhone()))
                    .toList();

            if (!records.isEmpty()) {
                PatientRecord record = records.get(0);
                record.markAsUnread(); // Using the new method from enhanced entity
                patientRecordRepository.save(record);
            }

            return ResponseEntity.ok(conversation);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete a conversation
     */
    @DeleteMapping("/conversations/{id}")
    public ResponseEntity<Void> deleteConversation(@PathVariable Long id) {
        if (conversationRepository.existsById(id)) {
            conversationRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Get patient record for a specific conversation
     */
    @GetMapping("/patient-records/conversation/{conversationId}")
    public ResponseEntity<PatientRecord> getPatientRecordByConversation(@PathVariable Long conversationId) {
        Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);

        if (conversationOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Conversation conversation = conversationOpt.get();

        // Find patient record for this user
        List<PatientRecord> records = patientRecordRepository.findAll().stream()
                .filter(pr -> pr.getPatient() != null &&
                        pr.getPatient().getMobilePhone().equals(conversation.getUser().getMobilePhone()))
                .toList();

        if (records.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(records.get(0));
    }

    /**
     * Get all patient records
     */
    @GetMapping("/patient-records")
    public List<PatientRecord> getAllPatientRecords() {
        return patientRecordRepository.findAll();
    }

    /**
     * Get patient record by ID
     */
    @GetMapping("/patient-records/{id}")
    public ResponseEntity<PatientRecord> getPatientRecord(@PathVariable Long id) {
        Optional<PatientRecord> record = patientRecordRepository.findById(id);
        return record.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}