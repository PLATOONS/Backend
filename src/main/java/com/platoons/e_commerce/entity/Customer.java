package com.platoons.e_commerce.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "customer")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Customer extends BaseEntity {
    @Id
    @UuidGenerator
    private String customerId;

    @OneToMany(mappedBy = "customer", fetch = FetchType.EAGER)
    private Set<Authority> authorities;

    @NotNull
    @Column(updatable = false)
    @CreatedDate
    private LocalDateTime registrationDate;

    @Size(min = 2, message = "Username must be at least 2 characters long")
    @Size(max = 32, message = "Username must be at most 32 characters long")
    @Column(unique = true)
    @NotNull
    private String username;

    @NotNull(message = "Password is required")
    private String passwordHash;

    @Email
    @Column(unique = true)
    @NotNull
    private String email;

    @Pattern(regexp = "^\\+([0-9]{1,4})[-\\s]?([0-9]{1,15})$", message = "Must be a valid phone number")
    private String phoneNumber;

    @NotNull(message = "First name is required")
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotNull(message = "Last name is required")
    @NotBlank(message = "Last name is required")
    private String lastName;
}
