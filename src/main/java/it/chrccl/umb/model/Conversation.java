package it.chrccl.umb.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // the user who started the conversation (the patient)
    @ManyToOne
    @JoinColumn(name = "user_mobile_phone", referencedColumnName = "mobile_phone")
    private User user;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "conversation", orphanRemoval = true)
    @JsonManagedReference   // 👈 Conversation serializes its messages
    private List<Message> messages = new ArrayList<>();

    public void addMessage(Message msg) {
        if (msg != null) {
            messages.add(msg);
            msg.setConversation(this);
        }
    }

}
