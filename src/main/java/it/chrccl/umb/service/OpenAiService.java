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
    }

    default ExtractionResult extractPatientRecordFromConversation(List<String> messages, User user) throws Exception {
        // Get injected config instead of creating new one
        OpenAIConfig config = getConfig();

        // Build a prompt that gives the model instructions and the conversation
        StringBuilder conv = new StringBuilder();
        for (String m : messages) {
            conv.append(m).append("\n");
        }

        String system = getSystemPrompt();
        String model = config.getModel();
        String apiKey = config.getApiKey();

        // Add validation for API key
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("OpenAI API key is not configured. Please set openai.api-key in application.properties");
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", model);

        // Using chat completion format
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
            // Navigate to choices[0].message.content
            String content = root.path("choices").get(0).path("message").path("content").asText();

            // Some models might wrap JSON in triple backticks. Try to extract JSON substring.
            String jsonText = extractJson(content);

            JsonNode json = mapper.readTree(jsonText);

            PatientRecord pr = PatientRecord.builder()
                    .patient(user)
                    .issue(getIssue())
                    .mainGoal(getText(json, "mainGoal"))
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
                    .estimatedBudget(getText(json, "estimatedBudget"))
                    .urgency(getText(json, "urgency"))
                    .interestLevel(getText(json, "interestLevel"))
                    .consultationRequested(getText(json, "consultationRequested"))
                    .otherNotes(getText(json, "otherNotes"))
                    .build();

            ExtractionResult er = new ExtractionResult();
            er.patientRecord = pr;
            er.reply = getText(json, "reply");

            return er;
        } catch (Exception e) {
            // Fallback response if OpenAI fails
            ExtractionResult fallback = new ExtractionResult();
            fallback.patientRecord = PatientRecord.builder()
                    .patient(user)
                    .issue(getIssue())
                    .build();
            fallback.reply = "Mi dispiace, ho avuto un problema tecnico. Puoi ripetere il tuo messaggio?";

            // Log the error for debugging
            System.err.println("OpenAI API call failed: " + e.getMessage());
            e.printStackTrace();

            return fallback;
        }
    }

    default String getText(JsonNode n, String key) {
        return n.has(key) && !n.get(key).isNull() ? n.get(key).asText() : "";
    }

    default String extractJson(String s) {
        int first = s.indexOf('{');
        int last = s.lastIndexOf('}');
        if (first >= 0 && last >= 0 && last > first) {
            return s.substring(first, last + 1);
        } else {
            return s;
        }
    }

    String getSystemPrompt();
    Issue getIssue();
    OpenAIConfig getConfig(); // New method to get injected config
}