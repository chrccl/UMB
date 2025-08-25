package it.chrccl.umb.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageId {

    @ManyToOne
    @JoinColumn(name = "from_mobile_phone", referencedColumnName = "mobile_phone")
    private User from;

    @ManyToOne
    @JoinColumn(name = "to_mobile_phone", referencedColumnName = "mobile_phone")
    private User to;

    private LocalDateTime timestamp;

}
