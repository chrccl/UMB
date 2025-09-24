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

            // CORREZIONE: Aggiorna il contactedDate sulla Conversation stessa
            conversation.setContactedDate(LocalDateTime.now());
            conversationRepository.save(conversation);

            // Aggiorna anche il PatientRecord se esiste
            Optional<PatientRecord> recordOpt = patientRecordRepository
                    .findByPatientMobilePhone(conversation.getUser().getMobilePhone());

            if (recordOpt.isPresent()) {
                PatientRecord record = recordOpt.get();
                record.markAsContacted();
                patientRecordRepository.save(record);
            }

            return ResponseEntity.ok(conversation);
        } catch (Exception e) {
            System.err.println("Error marking conversation as contacted: " + e.getMessage());
            e.printStackTrace();
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

            // CORREZIONE: Rimuovi il contactedDate dalla Conversation
            conversation.setContactedDate(null);
            conversationRepository.save(conversation);

            // Aggiorna anche il PatientRecord se esiste
            Optional<PatientRecord> recordOpt = patientRecordRepository
                    .findByPatientMobilePhone(conversation.getUser().getMobilePhone());

            if (recordOpt.isPresent()) {
                PatientRecord record = recordOpt.get();
                record.markAsUnread();
                patientRecordRepository.save(record);
            }

            return ResponseEntity.ok(conversation);
        } catch (Exception e) {
            System.err.println("Error marking conversation as unread: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Delete a conversation
     */
    @DeleteMapping("/conversations/{id}")
    public ResponseEntity<Void> deleteConversation(@PathVariable Long id) {
        try {
            if (conversationRepository.existsById(id)) {
                conversationRepository.deleteById(id);
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("Error deleting conversation: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get patient record for a specific conversation
     */
    @GetMapping("/patient-records/conversation/{conversationId}")
    public ResponseEntity<PatientRecord> getPatientRecordByConversation(@PathVariable Long conversationId) {
        try {
            Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);

            if (conversationOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Conversation conversation = conversationOpt.get();

            // Find patient record for this user using the new repository method
            Optional<PatientRecord> recordOpt = patientRecordRepository
                    .findByPatientMobilePhone(conversation.getUser().getMobilePhone());

            if (recordOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(recordOpt.get());
        } catch (Exception e) {
            System.err.println("Error retrieving patient record by conversation: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all patient records
     */
    @GetMapping("/patient-records")
    public List<PatientRecord> getAllPatientRecords() {
        try {
            return patientRecordRepository.findAll();
        } catch (Exception e) {
            System.err.println("Error retrieving all patient records: " + e.getMessage());
            e.printStackTrace();
            return List.of(); // Return empty list instead of throwing exception
        }
    }

    /**
     * Get patient record by ID
     */
    @GetMapping("/patient-records/{id}")
    public ResponseEntity<PatientRecord> getPatientRecord(@PathVariable Long id) {
        try {
            Optional<PatientRecord> record = patientRecordRepository.findById(id);
            return record.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.err.println("Error retrieving patient record by ID: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}