
package com.giftplatform.user.repository;

import com.giftplatform.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;


import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface UserRepository
        extends JpaRepository<User, UUID> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT u
    FROM User u
    WHERE u.id = :userId
    """)
    Optional<User> findForPhoneEnrollment(
            @Param("userId") UUID userId
    );

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumberAndIdNot(
            String phoneNumber,
            UUID userId
    );

}
