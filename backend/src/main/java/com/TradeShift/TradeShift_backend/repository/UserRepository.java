package com.TradeShift.TradeShift_backend.repository;
import com.TradeShift.TradeShift_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
     User findByEmail(String email);

}
