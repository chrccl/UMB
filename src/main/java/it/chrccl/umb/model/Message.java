package it.chrccl.umb.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;

    // Role could be "user" or "bot"
    private String role;

    @Column(name = "timestamp")
    private LocalDateTime timestamp = LocalDateTime.now();

    // Who sent this message
    @ManyToOne
    @JoinColumn(name = "from_mobile_phone", referencedColumnName = "mobile_phone")
    private User fromUser;

    // Who received this message (optional)
    @ManyToOne
    @JoinColumn(name = "to_mobile_phone", referencedColumnName = "mobile_phone")
    private User toUser;

    @ManyToOne
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    public Message(String text, String role, User fromUser, User toUser, Conversation conversation) {
        this.text = text;
        this.role = role;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.conversation = conversation;
        this.timestamp = LocalDateTime.now();
    }
}