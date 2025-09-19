package it.chrccl.umb.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_mobile_phone", referencedColumnName = "mobile_phone")
    private User patient;

    @Enumerated(EnumType.STRING)
    private Issue issue;

    // Contact tracking fields
    private LocalDateTime contactedDate;
    private Boolean isContacted = false;

    private String mainGoal;
    private String duration;
    private String locations;
    private String color;
    private String triggeringEvent;
    private String weightChanges;
    private String pastTreatments;
    private String treatmentResults;
    private String dietDescription;
    private String physicalActivity;
    private String knownDeficiencies;
    private String medications;
    private String smokingDrinking;
    private String estimatedBudget;
    private String urgency;
    private String interestLevel;
    private String consultationRequested;
    private String otherNotes;

    // Additional fields for better dashboard functionality
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }

    // Helper method to determine if patient was contacted
    public Boolean getIsContacted() {
        return this.contactedDate != null;
    }

    // Helper method to mark as contacted
    public void markAsContacted() {
        this.contactedDate = LocalDateTime.now();
        this.isContacted = true;
        this.updatedAt = LocalDateTime.now();
    }

    // Helper method to mark as unread
    public void markAsUnread() {
        this.contactedDate = null;
        this.isContacted = false;
        this.updatedAt = LocalDateTime.now();
    }

    // Helper method to update fields from another PatientRecord
    public void updateFrom(PatientRecord other) {
        if (other == null) return;
        
        // Only update non-null and non-empty values
        if (other.getMainGoal() != null && !other.getMainGoal().trim().isEmpty()) {
            this.mainGoal = other.getMainGoal();
        }
        if (other.getDuration() != null && !other.getDuration().trim().isEmpty()) {
            this.duration = other.getDuration();
        }
        if (other.getLocations() != null && !other.getLocations().trim().isEmpty()) {
            this.locations = other.getLocations();
        }
        if (other.getColor() != null && !other.getColor().trim().isEmpty()) {
            this.color = other.getColor();
        }
        if (other.getTriggeringEvent() != null && !other.getTriggeringEvent().trim().isEmpty()) {
            this.triggeringEvent = other.getTriggeringEvent();
        }
        if (other.getWeightChanges() != null && !other.getWeightChanges().trim().isEmpty()) {
            this.weightChanges = other.getWeightChanges();
        }
        if (other.getPastTreatments() != null && !other.getPastTreatments().trim().isEmpty()) {
            this.pastTreatments = other.getPastTreatments();
        }
        if (other.getTreatmentResults() != null && !other.getTreatmentResults().trim().isEmpty()) {
            this.treatmentResults = other.getTreatmentResults();
        }
        if (other.getDietDescription() != null && !other.getDietDescription().trim().isEmpty()) {
            this.dietDescription = other.getDietDescription();
        }
        if (other.getPhysicalActivity() != null && !other.getPhysicalActivity().trim().isEmpty()) {
            this.physicalActivity = other.getPhysicalActivity();
        }
        if (other.getKnownDeficiencies() != null && !other.getKnownDeficiencies().trim().isEmpty()) {
            this.knownDeficiencies = other.getKnownDeficiencies();
        }
        if (other.getMedications() != null && !other.getMedications().trim().isEmpty()) {
            this.medications = other.getMedications();
        }
        if (other.getSmokingDrinking() != null && !other.getSmokingDrinking().trim().isEmpty()) {
            this.smokingDrinking = other.getSmokingDrinking();
        }
        if (other.getEstimatedBudget() != null && !other.getEstimatedBudget().trim().isEmpty()) {
            this.estimatedBudget = other.getEstimatedBudget();
        }
        if (other.getUrgency() != null && !other.getUrgency().trim().isEmpty()) {
            this.urgency = other.getUrgency();
        }
        if (other.getInterestLevel() != null && !other.getInterestLevel().trim().isEmpty()) {
            this.interestLevel = other.getInterestLevel();
        }
        if (other.getConsultationRequested() != null && !other.getConsultationRequested().trim().isEmpty()) {
            this.consultationRequested = other.getConsultationRequested();
        }
        if (other.getOtherNotes() != null && !other.getOtherNotes().trim().isEmpty()) {
            this.otherNotes = other.getOtherNotes();
        }
        
        this.updatedAt = LocalDateTime.now();
    }
}