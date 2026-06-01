package com.shopsphere.auth_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_user_email", columnList = "email"), // Speeds up logins
                @Index(name = "idx_user_status", columnList = "status"), // Speeds up admin dashboard queries
                @Index(name = "idx_user_role", columnList = "role")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE users SET status = 'DELETED' WHERE id = ?") // Soft delete
@SQLRestriction("status <> 'DELETED'") // Globally hides deleted users
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Emails should be indexed and capped in length to prevent malicious massive payloads
    @Column(unique = true, nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 100) // BCrypt hashes are typically 60 characters
    private String password;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Enumerated(EnumType.STRING) // CRITICAL: Prevents database corruption
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    // --- AUDITING FIELDS ---

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    // --- METHODS ---

    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = UserStatus.ACTIVE; // Users are active by default upon registration
        }
    }

    // Safe equals and hashCode relying only on the DB ID to prevent infinite loops
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id != null && id.equals(user.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}