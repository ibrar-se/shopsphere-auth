package com.shopsphere.auth_service.repository;

import com.shopsphere.auth_service.model.Role;
import com.shopsphere.auth_service.model.User;
import com.shopsphere.auth_service.model.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 1. FOR LOGIN: Safely fetches the user (ignores DELETED users automatically)
    Optional<User> findByEmail(String email);

    // 2. FOR REGISTRATION: The absolute fastest way to validate uniqueness
    boolean existsByEmailIgnoreCase(String email);

    // 3. FOR ADMIN DASHBOARD: Fetch all users securely without loading passwords
    @Query("SELECT u.id AS id, u.email AS email, u.fullName AS fullName, u.role AS role, u.status AS status FROM User u")
    Page<UserSummary> findAllUserSummaries(Pageable pageable);

    // 4. FOR FILTERING: Allow admins to find users by a specific role
    @Query("SELECT u.id AS id, u.email AS email, u.fullName AS fullName, u.role AS role, u.status AS status FROM User u WHERE u.role = :role")
    Page<UserSummary> findByRole(Role role, Pageable pageable);

    /**
     * SECURITY PROJECTION: This guarantees that password hashes and heavy audit logs
     * are never accidentally loaded into memory or sent to the frontend during list queries.
     */
    interface UserSummary {
        Long getId();
        String getEmail();
        String getFullName();
        Role getRole();
        UserStatus getStatus();
    }
}