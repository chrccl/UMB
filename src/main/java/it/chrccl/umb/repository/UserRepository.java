package it.chrccl.umb.repository;

import it.chrccl.umb.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
