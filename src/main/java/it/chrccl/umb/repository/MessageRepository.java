package it.chrccl.umb.repository;

import it.chrccl.umb.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository <Message, Long> {
}
