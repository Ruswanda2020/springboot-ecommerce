package com.oneDev.ecommerce.repository;

import com.oneDev.ecommerce.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = """
       SELECT * FROM users
       WHERE username = :usernameOrEmail
       OR email = :usernameOrEmail
    """, nativeQuery = true)
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    @Query(value = """
       SELECT * FROM users
       WHERE lower(username) LIKE lower(CONCAT('%', :usernameOrEmail, '%')) 
          OR lower(email) LIKE lower(CONCAT('%', :usernameOrEmail, '%'))
""", nativeQuery = true)
    Page<User> searchUsers(@Param("usernameOrEmail") String usernameOrEmail, Pageable pageable);
}
