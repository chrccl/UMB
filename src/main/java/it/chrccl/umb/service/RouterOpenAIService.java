package it.chrccl.umb.service;

import it.chrccl.umb.config.OpenAIConfig;
import it.chrccl.umb.model.Issue;
import it.chrccl.umb.model.PatientRecord;
import it.chrccl.umb.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servizio di routing che analizza la conversazione e determina quale servizio specifico utilizzare.
 * Questo è il servizio di default che viene utilizzato all'inizio di ogni conversazione.
 */
@Service(Issue.ROUTER_SERVICE)
public class RouterOpenAIService implements OpenAiService {

    @Autowired
    private OpenAIConfig config;

    @Override
    public String getSystemPrompt() {
        return "Sei un assistente virtuale professionale di una clinica estetica e di longevità. "
                + "Il tuo PRIMO compito è capire per quale servizio il cliente ci sta contattando. "
                + "Abbiamo 3 servizi principali: "
                + "1. SMAGLIATURE (stretch marks) - Trattamento delle smagliature "
                + "2. LONGEVITY - Programmi di longevità, anti-aging, benessere generale "
                + "3. MICROLIPOSUZIONE - Rimozione del grasso localizzato "
                + "Analizza attentamente il messaggio del cliente e cerca di capire di quale servizio ha bisogno. "
                + "Se il messaggio menziona chiaramente uno dei servizi (es. 'smagliature', 'longevity', 'anti-aging', 'liposuzione', 'grasso localizzato'), "
                + "imposta il campo 'detectedService' con uno di questi valori: STRETCHMARKS, LONGEVITY, MICROLIPOSUCTION. "
                + "Se NON è chiaro quale servizio vuole, rispondi con un messaggio cordiale che chiede quale servizio lo interessa, "
                + "presentando le 3 opzioni in modo semplice e accattivante. In questo caso, lascia 'detectedService' vuoto. "
                + "Sii sempre cortese, professionale e accogliente. "
                + "Risposta in formato JSON con chiavi: detectedService, reply. "
                + "Il campo 'reply' deve SEMPRE contenere una risposta valida. "
                + "Il campo 'detectedService' può essere: STRETCHMARKS, LONGEVITY, MICROLIPOSUCTION, o stringa vuota se non chiaro. "
                + "**Solo output JSON.**";
    }

    @Override
    public Issue getIssue() {
        // Il router non ha un issue specifico, ritorna null
        return null;
    }

    @Override
    public OpenAIConfig getConfig() {
        return config;
    }

    /**
     * Override del metodo di estrazione per gestire il routing
     */
    @Override
    public ExtractionResult extractPatientRecordFromConversation(List<String> messages, User user) throws Exception {
        ExtractionResult result = OpenAiService.super.extractPatientRecordFromConversation(messages, user);

        // Il PatientRecord del router non ha un issue specifico ancora
        if (result.patientRecord != null) {
            result.patientRecord.setIssue(null);
        }

        return result;
    }
}