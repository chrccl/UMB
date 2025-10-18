package it.chrccl.umb.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import it.chrccl.umb.model.*;
import it.chrccl.umb.repository.ConversationRepository;
import it.chrccl.umb.repository.MessageRepository;
import it.chrccl.umb.repository.PatientRecordRepository;
import it.chrccl.umb.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ChatbotService {

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final PatientRecordRepository patientRecordRepository;
    private final ApplicationContext applicationContext;

    private final String twilioSid;
    private final String twilioAuth;
    private final String twilioFrom;
    private final String twilioMSGServiceId;

    // Cache per memorizzare quale servizio usare per ogni utente
    private final Map<String, String> userServiceCache = new HashMap<>();

    public ChatbotService(
            UserRepository userRepository,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            PatientRecordRepository patientRecordRepository,
            ApplicationContext applicationContext,
            @Value("${twilio.accountSid}") String twilioSid,
            @Value("${twilio.authToken}") String twilioAuth,
            @Value("${twilio.fromNumber}") String twilioFrom,
            @Value("${twilio.messaging.sid}") String twilioMSGServiceId
    ) {
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.patientRecordRepository = patientRecordRepository;
        this.applicationContext = applicationContext;
        this.twilioSid = twilioSid;
        this.twilioAuth = twilioAuth;
        this.twilioFrom = twilioFrom;
        this.twilioMSGServiceId = twilioMSGServiceId;
    }

    @PostConstruct
    public void initTwilio() {
        Twilio.init(twilioSid, twilioAuth);
    }

    /**
     * Handle an incoming user message with intelligent routing.
     * Usa il RouterService per capire quale servizio utilizzare.
     */
    @Transactional
    public void handleIncomingWhatsapp(String fromWhatsappNumber, String incomingText) throws Exception {
        String userPhone = fromWhatsappNumber.replace("whatsapp:", "");

        // Find or create user
        User user = userRepository.findById(userPhone).orElseGet(() -> {
            User u = new User();
            u.setMobilePhone(userPhone);
            u.setFullName("");
            return userRepository.save(u);
        });

        // Create or find bot user
        User botUser = userRepository.findById(twilioFrom.replace("whatsapp:", "")).orElseGet(() -> {
            User b = new User();
            b.setMobilePhone(twilioFrom.replace("whatsapp:", ""));
            b.setFullName("Bot");
            return userRepository.save(b);
        });

        // Find or create conversation
        Conversation conv = conversationRepository.findAll().stream()
                .filter(c -> c.getUser() != null && userPhone.equals(c.getUser().getMobilePhone()))
                .findFirst()
                .orElseGet(() -> {
                    Conversation c = new Conversation();
                    c.setUser(user);
                    return conversationRepository.save(c);
                });

        // Save incoming message
        it.chrccl.umb.model.Message userMsg = new it.chrccl.umb.model.Message(
                incomingText, "user", user, botUser, conv
        );
        conv.addMessage(userMsg);
        messageRepository.save(userMsg);

        // Build conversation history
        List<String> convoTexts = new ArrayList<>();
        for (it.chrccl.umb.model.Message m : conv.getMessages()) {
            String role = m.getRole() == null ? "user" : m.getRole();
            String who = role.equals("user") ? "USER" : "BOT";
            convoTexts.add(who + ": " + m.getText());
        }

        // Determina quale servizio usare
        String serviceToUse = determineService(userPhone, convoTexts, user);

        System.out.println("Using service: " + serviceToUse + " for user: " + userPhone);

        // Get the appropriate OpenAI service
        OpenAiService openAiService = getOpenAiService(serviceToUse);
        if (openAiService == null) {
            throw new IllegalStateException("No OpenAiService found for: " + serviceToUse);
        }

        // Call OpenAI to extract info and get reply
        OpenAiService.ExtractionResult result = openAiService.extractPatientRecordFromConversation(convoTexts, user);

        // Se il router ha rilevato un servizio specifico, aggiorna la cache
        if (result.detectedService != null && !result.detectedService.isEmpty()) {
            String detectedService = mapDetectedServiceToServiceId(result.detectedService);
            if (detectedService != null) {
                userServiceCache.put(userPhone, detectedService);
                System.out.println("Router detected service: " + detectedService + " for user: " + userPhone);

                // Se abbiamo appena rilevato il servizio, richiamiamo con il servizio corretto
                openAiService = getOpenAiService(detectedService);
                result = openAiService.extractPatientRecordFromConversation(convoTexts, user);
            }
        }

        // Save/Update patient record
        PatientRecord pr = result.patientRecord;
        if (pr != null && pr.getIssue() != null) {
            userRepository.save(pr.getPatient());

            Optional<PatientRecord> existingRecord = patientRecordRepository
                    .findByPatientAndIssue(pr.getPatient(), pr.getIssue());

            if (existingRecord.isPresent()) {
                PatientRecord existing = existingRecord.get();
                existing.updateFrom(pr);
                patientRecordRepository.save(existing);
                System.out.println("Updated existing PatientRecord for patient: " + pr.getPatient().getMobilePhone());
            } else {
                patientRecordRepository.save(pr);
                System.out.println("Created new PatientRecord for patient: " + pr.getPatient().getMobilePhone());
            }
        }

        // Save bot message
        it.chrccl.umb.model.Message botMessage = new it.chrccl.umb.model.Message(
                result.reply, "bot", botUser, user, conv
        );
        conv.addMessage(botMessage);
        messageRepository.save(botMessage);

        // Save conversation
        conversationRepository.save(conv);

        // Send via Twilio
        sendWhatsappMessage(userPhone, result.reply);
    }

    /**
     * Determina quale servizio usare per questo utente.
     * Se l'utente ha già un servizio assegnato nella cache, usa quello.
     * Altrimenti, usa il RouterService.
     */
    private String determineService(String userPhone, List<String> convoTexts, User user) {
        // Se abbiamo già determinato il servizio per questo utente, usalo
        if (userServiceCache.containsKey(userPhone)) {
            return userServiceCache.get(userPhone);
        }

        // Altrimenti, usa il router
        return Issue.ROUTER_SERVICE;
    }

    /**
     * Mappa il servizio rilevato dal router al service ID corretto.
     */
    private String mapDetectedServiceToServiceId(String detectedService) {
        return switch (detectedService.toUpperCase()) {
            case "STRETCHMARKS" -> Issue.STRETCHMARKS_SERVICE;
            case "LONGEVITY" -> Issue.LONGEVITY_SERVICE;
            case "MICROLIPOSUCTION" -> Issue.MICROLIPOSUCTION_SERVICE;
            default -> null;
        };
    }

    private void sendWhatsappMessage(String toPhoneWithoutPrefix, String body) {
        String to = "whatsapp:" + toPhoneWithoutPrefix;
        MessageCreator creator = Message.creator(
                new PhoneNumber(to),
                this.twilioMSGServiceId,
                body
        );
        creator.create();
    }

    private OpenAiService getOpenAiService(String serviceId) {
        try {
            return (OpenAiService) applicationContext.getBean(serviceId);
        } catch (Exception e) {
            System.err.println("Failed to get service: " + serviceId + ", error: " + e.getMessage());
            return null;
        }
    }
}