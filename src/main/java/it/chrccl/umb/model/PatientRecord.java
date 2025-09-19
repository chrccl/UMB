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

    // Original fields from your existing model
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
}