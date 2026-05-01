package com.booking.auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    private String description;
    
    public static final String SUPER_ADMIN = "ROLE_SUPER_ADMIN";
    public static final String BUSINESS_ADMIN = "ROLE_BUSINESS_ADMIN";
    public static final String ADMIN = "ROLE_ADMIN";
    public static final String USER = "ROLE_USER";
}
