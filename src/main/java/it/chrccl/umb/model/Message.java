package it.chrccl.umb.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {

    @EmbeddedId
    private MessageId id;

    private String text;

    // Role could be "user" or "bot" (optional)
    private String role;

    @ManyToOne
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

}
