package it.chrccl.umb.service;

import it.chrccl.umb.config.OpenAIConfig;
import it.chrccl.umb.model.Issue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service(Issue.STRETCHMARKS_SERVICE)
public class StretchMarksOpenAIService implements OpenAiService {

    @Autowired
    private OpenAIConfig config;

    @Override
    public String getSystemPrompt() {
        return "You are an assistant that extracts patient information about stretch marks (smagliature)."
                + " Given the conversation, output exactly ONE JSON object with keys: mainGoal, duration, locations, color, triggeringEvent, weightChanges, pastTreatments, treatmentResults, dietDescription, physicalActivity, knownDeficiencies, medications, smokingDrinking, otherNotes, and reply."
                + " 'reply' should be a short empathetic message to the user (in Italian if the messages are in Italian) summarizing next steps and optionally asking follow-up questions."
                + " If a field is unknown, make it an empty string. **Output JSON only.**";
    }

    @Override
    public Issue getIssue() {
        return Issue.STRETCHMARKS;
    }

    @Override
    public OpenAIConfig getConfig() {
        return config;
    }
}