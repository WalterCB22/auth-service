package com.booking.auth.service;

import com.booking.auth.dto.AuthResponse;
import com.booking.auth.dto.LoginRequest;
import com.booking.auth.dto.RegisterRequest;
import com.booking.auth.model.Role;
import com.booking.auth.model.User;
import com.booking.auth.repository.RoleRepository;
import com.booking.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthRESTService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder; // Inyectado desde el AuthorizationServerConfig

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("El username ya está en uso");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está en uso");
        }

        Role userRole = roleRepository.findByName(Role.USER)
                .orElseThrow(() -> new RuntimeException("Rol USER no encontrado"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .roles(Set.of(userRole))
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        userRepository.save(user);
        log.info("Usuario registrado nativamente: {}", user.getUsername());

        String jwtToken = generateTokenConRsa(user);
        return buildAuthResponse(user, jwtToken);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseGet(() -> userRepository.findByEmail(request.getUsername()).orElseThrow());

        String jwtToken = generateTokenConRsa(user);
        log.info("Usuario autenticado vía JSON: {}", user.getUsername());

        return buildAuthResponse(user, jwtToken);
    }

    private String generateTokenConRsa(User user) {
        Instant now = Instant.now();
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
                .issuer("http://localhost:9000")
                .issuedAt(now)
                .expiresAt(now.plus(1, ChronoUnit.DAYS))
                .subject(user.getUsername())
                .claim("roles", roles)
                .claim("user_id", user.getId().toString())
                .claim("email", user.getEmail());
                
        if (user.getBusinessId() != null) {
            builder.claim("business_id", user.getBusinessId().toString());
        }
        
        JwtClaimsSet claims = builder.build();
        
        // Creamos explícitamente el Header marcando que usamos algoritmo RSA 256.
        // Esto obliga a Nimbus a inyectar el campo 'kid' (Key ID) en el token.
        org.springframework.security.oauth2.jwt.JwsHeader jwsHeader = 
            org.springframework.security.oauth2.jwt.JwsHeader.with(
                org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256).build();
        
        // El JwtEncoder buscará el key adecuado y firmará
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(86400) // 1 día en segundos
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .userId(user.getId())
                .businessId(user.getBusinessId())
                .roles(roleNames)
                .build();
    }
}
