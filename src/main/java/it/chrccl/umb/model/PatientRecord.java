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

    // === CAMPI COMUNI ===
    private String mainGoal;
    private String estimatedBudget;
    private String urgency;
    private String interestLevel;
    private String consultationRequested;
    private String preferredContactTime;
    private String otherNotes;

    // === CAMPI SMAGLIATURE ===
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

    // === CAMPI LONGEVITY ===
    private String city;
    private String lifestyle;
    private String medicalConditions;
    private String mainProblem; // Infiammazioni, stress ossidativo, etc.

    // === CAMPI MICROLIPOSUZIONE ===
    private String treatmentAreas; // Zone da trattare
    private String height; // in cm
    private String weight; // in kg
    private String weightStable; // Peso stabile da almeno 6 mesi?
    private String allergies;
    private String bloodCoagulation; // Problemi di coagulazione
    private String preexistingConditions;
    private String photosSent; // Se ha inviato foto

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

    public Boolean getIsContacted() {
        return this.contactedDate != null;
    }

    public void markAsContacted() {
        this.contactedDate = LocalDateTime.now();
        this.isContacted = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsUnread() {
        this.contactedDate = null;
        this.isContacted = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateFrom(PatientRecord other) {
        if (other == null) return;

        // Common fields
        updateField(other.getMainGoal(), val -> this.mainGoal = val);
        updateField(other.getEstimatedBudget(), val -> this.estimatedBudget = val);
        updateField(other.getUrgency(), val -> this.urgency = val);
        updateField(other.getInterestLevel(), val -> this.interestLevel = val);
        updateField(other.getConsultationRequested(), val -> this.consultationRequested = val);
        updateField(other.getPreferredContactTime(), val -> this.preferredContactTime = val);
        updateField(other.getOtherNotes(), val -> this.otherNotes = val);

        // Stretch marks fields
        updateField(other.getDuration(), val -> this.duration = val);
        updateField(other.getLocations(), val -> this.locations = val);
        updateField(other.getColor(), val -> this.color = val);
        updateField(other.getTriggeringEvent(), val -> this.triggeringEvent = val);
        updateField(other.getWeightChanges(), val -> this.weightChanges = val);
        updateField(other.getPastTreatments(), val -> this.pastTreatments = val);
        updateField(other.getTreatmentResults(), val -> this.treatmentResults = val);
        updateField(other.getDietDescription(), val -> this.dietDescription = val);
        updateField(other.getPhysicalActivity(), val -> this.physicalActivity = val);
        updateField(other.getKnownDeficiencies(), val -> this.knownDeficiencies = val);
        updateField(other.getMedications(), val -> this.medications = val);
        updateField(other.getSmokingDrinking(), val -> this.smokingDrinking = val);

        // Longevity fields
        updateField(other.getCity(), val -> this.city = val);
        updateField(other.getLifestyle(), val -> this.lifestyle = val);
        updateField(other.getMedicalConditions(), val -> this.medicalConditions = val);
        updateField(other.getMainProblem(), val -> this.mainProblem = val);

        // Microliposuction fields
        updateField(other.getTreatmentAreas(), val -> this.treatmentAreas = val);
        updateField(other.getHeight(), val -> this.height = val);
        updateField(other.getWeight(), val -> this.weight = val);
        updateField(other.getWeightStable(), val -> this.weightStable = val);
        updateField(other.getAllergies(), val -> this.allergies = val);
        updateField(other.getBloodCoagulation(), val -> this.bloodCoagulation = val);
        updateField(other.getPreexistingConditions(), val -> this.preexistingConditions = val);
        updateField(other.getPhotosSent(), val -> this.photosSent = val);

        this.updatedAt = LocalDateTime.now();
    }

    private void updateField(String value, java.util.function.Consumer<String> setter) {
        if (value != null && !value.trim().isEmpty()) {
            setter.accept(value);
        }
    }
}