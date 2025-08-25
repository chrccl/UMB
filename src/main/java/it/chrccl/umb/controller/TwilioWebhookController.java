package it.chrccl.umb.controller;

import it.chrccl.umb.service.ChatbotService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/umb")
public class TwilioWebhookController {

    private final ChatbotService chatbotService;

    public TwilioWebhookController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @GetMapping("/")
    public String home() {
        return "Hello World";
    }

    // Twilio posts form-encoded params: From, Body, To
    // Example webhook URL to configure in Twilio: https://your-host/umb/webhook/STRETCHMARKS
    @PostMapping(value = "/webhook/{serviceId}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> incoming(@PathVariable("serviceId") String openAIServiceId,
                                           @RequestParam("From") String from,
                                           @RequestParam("Body") String body) {
        try {
            chatbotService.handleIncomingWhatsapp(from, body, openAIServiceId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok().build();

    }
}