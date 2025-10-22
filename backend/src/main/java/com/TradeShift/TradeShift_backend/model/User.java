package com.TradeShift.TradeShift_backend.model;

import com.TradeShift.TradeShift_backend.domain.USER_ROLE;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails; // Import UserDetails
import java.util.Collection;
import java.util.List;

@Entity
@Data
// The class implements UserDetails to satisfy Spring Security requirements
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String fullName;

    // Must be unique for UserDetails to use as the username
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Embedded
    private TwoFactorAuth twoFactorAuth = new TwoFactorAuth();

    @Enumerated(EnumType.STRING) // Ensures the role is stored as a string
    @Column(columnDefinition = "varchar(255) default 'ROLE_CUSTOMER'") // Default value for schema generation
    private USER_ROLE role = USER_ROLE.ROLE_CUSTOMER;

    // We will manage the 'enabled' state inside the UserDetails methods
    // If you need a field to store account status, add it back, but let's test without it first.
    // private boolean enabled = true;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Portfolio portfolio;


    // ---------------------------------------------
    // Implementation of UserDetails methods
    // ---------------------------------------------

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // This grants the user a single authority based on their defined role (e.g., ROLE_CUSTOMER)
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        // Returns the hashed password stored in the database
        return this.password;
    }

    @Override
    public String getUsername() {
        // The email is used as the unique username for authentication
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Defaulting to true, but if you re-introduce a private 'enabled' field,
        // return that field here (e.g., 'return this.enabled;').
        return true;
    }
}
