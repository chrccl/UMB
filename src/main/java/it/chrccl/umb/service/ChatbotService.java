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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ChatbotService {

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final PatientRecordRepository patientRecordRepository;

    // map of all OpenAiService beans keyed by bean name (component name)
    private final Map<String, OpenAiService> openAiServices;

    private final String twilioSid;
    private final String twilioAuth;
    private final String twilioFrom;
    private final String twilioMSGServiceId;

    public ChatbotService(
            UserRepository userRepository,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            PatientRecordRepository patientRecordRepository,
            // Spring injects all beans implementing OpenAiService as a Map<beanName, bean>
            Map<String, OpenAiService> openAiServices,
            @Value("${twilio.accountSid}") String twilioSid,
            @Value("${twilio.authToken}") String twilioAuth,
            @Value("${twilio.fromNumber}") String twilioFrom,
            @Value("${twilio.messaging.sid}") String twilioMSGServiceId
    ) {
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.patientRecordRepository = patientRecordRepository;
        this.openAiServices = Map.copyOf(openAiServices);
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
     * Handle an incoming user message (from WhatsApp/Twilio).
     * This will: find/create user, find/create conversation, persist incoming message, call OpenAI, persist patientRecord and bot message, send message via Twilio.
     */
    @Transactional
    public void handleIncomingWhatsapp(String fromWhatsappNumber, String incomingText, String openAiServiceID) throws Exception {
        OpenAiService openAiService = openAiServices.get(openAiServiceID);
        if (openAiService == null) throw new IllegalStateException("No OpenAiService implementations available");

        // Normalise numbers: Twilio uses "whatsapp:+123..."
        String userPhone = fromWhatsappNumber.replace("whatsapp:", "");

        // find or create user
        User user = userRepository.findById(userPhone).orElseGet(() -> {
            User u = new User();
            u.setMobilePhone(userPhone);
            u.setFullName("");
            return userRepository.save(u);
        });

        // Create or find bot user FIRST to avoid null references
        User botUser = userRepository.findById(twilioFrom.replace("whatsapp:", "")).orElseGet(() -> {
            User b = new User();
            b.setMobilePhone(twilioFrom.replace("whatsapp:", ""));
            b.setFullName("Bot");
            return userRepository.save(b);
        });

        // find or create a conversation for this user (simple strategy: newest conversation or new)
        Conversation conv = conversationRepository.findAll().stream()
                .filter(c -> c.getUser() != null && userPhone.equals(c.getUser().getMobilePhone()))
                .findFirst()
                .orElseGet(() -> {
                    Conversation c = new Conversation();
                    c.setUser(user);
                    return conversationRepository.save(c);
                });

        // save incoming message using simplified Message entity
        it.chrccl.umb.model.Message userMsg = new it.chrccl.umb.model.Message(
                incomingText, "user", user, botUser, conv
        );
        conv.addMessage(userMsg);
        messageRepository.save(userMsg);

        // Build conversation text for model (last N messages)
        List<String> convoTexts = new ArrayList<>();
        for (it.chrccl.umb.model.Message m : conv.getMessages()) {
            String role = m.getRole() == null ? "user" : m.getRole();
            String who = role.equals("user") ? "USER" : "BOT";
            convoTexts.add(who + ": " + m.getText());
        }

        // Call OpenAI to extract patient info and produce a reply
        OpenAiService.ExtractionResult result = openAiService.extractPatientRecordFromConversation(convoTexts, user);

        // Save patient record if not empty
        PatientRecord pr = result.patientRecord;
        if (pr != null) {
            // Make sure patient reference is properly set
            pr.setPatient(user);
            patientRecordRepository.save(pr);
        }

        // Save bot message using simplified Message entity
        it.chrccl.umb.model.Message botMessage = new it.chrccl.umb.model.Message(
                result.reply, "bot", botUser, user, conv
        );
        conv.addMessage(botMessage);
        messageRepository.save(botMessage);

        // Save conversation with all messages
        conversationRepository.save(conv);

        // send via Twilio
        sendWhatsappMessage(userPhone, result.reply);
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
}