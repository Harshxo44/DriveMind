package com.guardian.backend.diagnostics;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "diagnostic_trouble_codes")
public class DiagnosticTroubleCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(nullable = false)
    private String system;

    @Column(nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String symptoms;

    @Column(columnDefinition = "TEXT")
    private String potentialCauses;

    @Column(columnDefinition = "TEXT")
    private String recommendedAction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity = Severity.MODERATE;

    public DiagnosticTroubleCode() {}

    public DiagnosticTroubleCode(UUID id, String code, String system, String description, String symptoms, String potentialCauses, String recommendedAction, Severity severity) {
        this.id = id;
        this.code = code;
        this.system = system;
        this.description = description;
        this.symptoms = symptoms;
        this.potentialCauses = potentialCauses;
        this.recommendedAction = recommendedAction;
        this.severity = severity != null ? severity : Severity.MODERATE;
    }

    public static DiagnosticTroubleCodeBuilder builder() {
        return new DiagnosticTroubleCodeBuilder();
    }

    public static class DiagnosticTroubleCodeBuilder {
        private UUID id;
        private String code;
        private String system;
        private String description;
        private String symptoms;
        private String potentialCauses;
        private String recommendedAction;
        private Severity severity = Severity.MODERATE;

        public DiagnosticTroubleCodeBuilder id(UUID id) { this.id = id; return this; }
        public DiagnosticTroubleCodeBuilder code(String code) { this.code = code; return this; }
        public DiagnosticTroubleCodeBuilder system(String system) { this.system = system; return this; }
        public DiagnosticTroubleCodeBuilder description(String description) { this.description = description; return this; }
        public DiagnosticTroubleCodeBuilder symptoms(String symptoms) { this.symptoms = symptoms; return this; }
        public DiagnosticTroubleCodeBuilder potentialCauses(String potentialCauses) { this.potentialCauses = potentialCauses; return this; }
        public DiagnosticTroubleCodeBuilder recommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; return this; }
        public DiagnosticTroubleCodeBuilder severity(Severity severity) { this.severity = severity; return this; }

        public DiagnosticTroubleCode build() {
            return new DiagnosticTroubleCode(id, code, system, description, symptoms, potentialCauses, recommendedAction, severity);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getSystem() { return system; }
    public void setSystem(String system) { this.system = system; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
    public String getPotentialCauses() { return potentialCauses; }
    public void setPotentialCauses(String potentialCauses) { this.potentialCauses = potentialCauses; }
    public String getRecommendedAction() { return recommendedAction; }
    public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public enum Severity {
        LOW,
        MODERATE,
        HIGH,
        CRITICAL
    }
}
