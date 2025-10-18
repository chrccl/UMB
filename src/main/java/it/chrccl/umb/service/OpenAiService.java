package it.chrccl.umb.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.chrccl.umb.config.OpenAIConfig;
import it.chrccl.umb.model.Issue;
import it.chrccl.umb.model.PatientRecord;
import it.chrccl.umb.model.User;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface OpenAiService {

    ObjectMapper mapper = new ObjectMapper();

    class ExtractionResult {
        public PatientRecord patientRecord;
        public String reply;
        public String detectedService; // Campo per il routing
    }

    default ExtractionResult extractPatientRecordFromConversation(List<String> messages, User user) throws Exception {
        OpenAIConfig config = getConfig();

        StringBuilder conv = new StringBuilder();
        for (String m : messages) {
            conv.append(m).append("\n");
        }

        String system = getSystemPrompt();
        String model = config.getModel();
        String apiKey = config.getApiKey();

        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("OpenAI API key is not configured. Please set openai.api-key in application.properties");
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", model);

        List<Map<String, String>> messagesPayload = new ArrayList<>();
        messagesPayload.add(Map.of("role", "system", "content", system));
        messagesPayload.add(Map.of("role", "user", "content", conv.toString()));

        payload.put("messages", messagesPayload);
        payload.put("temperature", 0.0);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(payload), headers);
        RestTemplate rt = new RestTemplate();

        try {
            ResponseEntity<String> resp = rt.postForEntity("https://api.openai.com/v1/chat/completions", entity, String.class);

            if (resp.getStatusCode() != HttpStatus.OK) {
                throw new IllegalStateException("OpenAI error: " + resp.getStatusCode() + " " + resp.getBody());
            }

            JsonNode root = mapper.readTree(resp.getBody());
            String content = root.path("choices").get(0).path("message").path("content").asText();
            String jsonText = extractJson(content);
            JsonNode json = mapper.readTree(jsonText);

            // Aggiorna i campi User
            boolean userUpdated = false;

            if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
                String fullName = getText(json, "userFullName");
                if (!fullName.isEmpty()) {
                    user.setFullName(fullName);
                    userUpdated = true;
                }
            }

            if (user.getAge() == null || user.getAge().trim().isEmpty()) {
                String age = getText(json, "userAge");
                if (!age.isEmpty()) {
                    user.setAge(age);
                    userUpdated = true;
                }
            }

            if (user.getSex() == null || user.getSex().trim().isEmpty()) {
                String sex = getText(json, "userGender");
                if (!sex.isEmpty()) {
                    user.setSex(sex);
                    userUpdated = true;
                }
            }

            if (userUpdated) {
                System.out.println("Updated user info: Name=" + user.getFullName() +
                        ", Age=" + user.getAge() + ", Sex=" + user.getSex());
            }

            // Costruisci PatientRecord con tutti i campi possibili
            PatientRecord pr = PatientRecord.builder()
                    .patient(user)
                    .issue(getIssue())
                    // Common fields
                    .mainGoal(getText(json, "mainGoal"))
                    .estimatedBudget(getText(json, "estimatedBudget"))
                    .urgency(getText(json, "urgency"))
                    .interestLevel(getText(json, "interestLevel"))
                    .consultationRequested(getText(json, "consultationRequested"))
                    .preferredContactTime(getText(json, "preferredContactTime"))
                    .otherNotes(getText(json, "otherNotes"))
                    // Stretch marks fields
                    .duration(getText(json, "duration"))
                    .locations(getText(json, "locations"))
                    .color(getText(json, "color"))
                    .triggeringEvent(getText(json, "triggeringEvent"))
                    .weightChanges(getText(json, "weightChanges"))
                    .pastTreatments(getText(json, "pastTreatments"))
                    .treatmentResults(getText(json, "treatmentResults"))
                    .dietDescription(getText(json, "dietDescription"))
                    .physicalActivity(getText(json, "physicalActivity"))
                    .knownDeficiencies(getText(json, "knownDeficiencies"))
                    .medications(getText(json, "medications"))
                    .smokingDrinking(getText(json, "smokingDrinking"))
                    // Longevity fields
                    .city(getText(json, "city"))
                    .lifestyle(getText(json, "lifestyle"))
                    .medicalConditions(getText(json, "medicalConditions"))
                    .mainProblem(getText(json, "mainProblem"))
                    // Microliposuction fields
                    .treatmentAreas(getText(json, "treatmentAreas"))
                    .height(getText(json, "height"))
                    .weight(getText(json, "weight"))
                    .weightStable(getText(json, "weightStable"))
                    .allergies(getText(json, "allergies"))
                    .bloodCoagulation(getText(json, "bloodCoagulation"))
                    .preexistingConditions(getText(json, "preexistingConditions"))
                    .photosSent(getText(json, "photosSent"))
                    .build();

            ExtractionResult er = new ExtractionResult();
            er.patientRecord = pr;

            String reply = getText(json, "reply");
            if (reply == null || reply.trim().isEmpty()) {
                reply = "Grazie per il tuo messaggio. Come posso aiutarti ulteriormente?";
            }
            er.reply = reply;

            // Estrai detectedService per il routing
            er.detectedService = getText(json, "detectedService");

            return er;
        } catch (Exception e) {
            ExtractionResult fallback = new ExtractionResult();
            fallback.patientRecord = PatientRecord.builder()
                    .patient(user)
                    .issue(getIssue())
                    .build();
            fallback.reply = "Mi dispiace, ho avuto un problema tecnico. Puoi ripetere il tuo messaggio?";
            fallback.detectedService = "";

            System.err.println("OpenAI API call failed: " + e.getMessage());
            e.printStackTrace();

            return fallback;
        }
    }

    default String getText(JsonNode n, String key) {
        if (n == null || !n.has(key) || n.get(key).isNull()) {
            return "";
        }
        String text = n.get(key).asText();
        return text != null ? text.trim() : "";
    }

    default String extractJson(String s) {
        if (s == null) return "{}";

        int first = s.indexOf('{');
        int last = s.lastIndexOf('}');
        if (first >= 0 && last >= 0 && last > first) {
            return s.substring(first, last + 1);
        } else {
            return "{}";
        }
    }

    String getSystemPrompt();
    Issue getIssue();
    OpenAIConfig getConfig();
}