package it.chrccl.umb.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_mobile_phone", referencedColumnName = "mobile_phone")
    private User patient;

    private Issue issue;

    // Fields inspired by your doc: duration, location, color, cause, treatments, diet, activity ...
    private String mainGoal; // e.g. make less visible / prevent new
    private String duration; // e.g. "1 year"
    private String locations; // comma-separated or normalized as own table
    private String color; // "red/violaceo" or "white/silver"
    private String triggeringEvent; // pregnancy, growth, weight-change etc
    private String weightChanges; // yes/no + notes
    private String pastTreatments; // text
    private String treatmentResults; // text
    private String dietDescription;
    private String physicalActivity;
    private String knownDeficiencies;
    private String medications; // e.g. cortisone
    private String smokingDrinking;
    private String otherNotes;

}