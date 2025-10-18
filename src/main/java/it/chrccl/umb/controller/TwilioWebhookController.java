package it.chrccl.umb.controller;

import it.chrccl.umb.service.ChatbotService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller per gestire i webhook di Twilio.
 * Ora usa un routing intelligente automatico basato sul contenuto del messaggio.
 */
@RestController
@RequestMapping("/umb")
public class TwilioWebhookController {

    private final ChatbotService chatbotService;

    public TwilioWebhookController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @GetMapping("/")
    public String home() {
        return "UMB Chatbot Service - Ready";
    }

    /**
     * Webhook unificato per Twilio.
     * Il servizio corretto viene determinato automaticamente dal contenuto della conversazione.
     *
     * URL da configurare in Twilio: https://your-host/umb/webhook
     *
     * @param from Il numero WhatsApp del mittente (formato: whatsapp:+1234567890)
     * @param body Il contenuto del messaggio
     * @return Risposta HTTP 200 OK
     */
    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> incoming(@RequestParam("From") String from,
                                           @RequestParam("Body") String body) {
        try {
            System.out.println("Received message from: " + from + " - Content: " + body);
            chatbotService.handleIncomingWhatsapp(from, body);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Error processing webhook: " + e.getMessage());
            e.printStackTrace();
            // Ritorna 200 comunque per evitare retry da Twilio
            return ResponseEntity.ok().build();
        }
    }
}