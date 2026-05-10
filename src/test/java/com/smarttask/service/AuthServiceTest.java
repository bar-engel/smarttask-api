package com.smarttask.service;

import com.smarttask.exception.UserAlreadyExistsException;
import com.smarttask.model.dto.request.LoginRequest;
import com.smarttask.model.dto.request.RegisterRequest;
import com.smarttask.model.dto.response.AuthResponse;
import com.smarttask.model.entity.Role;
import com.smarttask.model.entity.User;
import com.smarttask.repository.UserRepository;
import com.smarttask.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks private AuthService authService;

    @Test
    void register_Success() {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .email("newuser@test.com")
                .password("password123")
                .build();

        User savedUser = User.builder()
                .id(1L)
                .username("newuser")
                .email("newuser@test.com")
                .password("encoded_password")
                .role(Role.USER)
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any())).thenReturn("jwt_token");

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt_token");
        assertThat(response.getUsername()).isEqualTo("newuser");
        assertThat(response.getRole()).isEqualTo("USER");
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void register_UsernameAlreadyExists_ThrowsException() {
        RegisterRequest request = RegisterRequest.builder()
                .username("existinguser")
                .email("new@test.com")
                .password("password123")
                .build();

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("username")
                .hasMessageContaining("existinguser");
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_EmailAlreadyExists_ThrowsException() {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .email("existing@test.com")
                .password("password123")
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("email")
                .hasMessageContaining("existing@test.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_Success() {
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .password("encoded_password")
                .role(Role.USER)
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("testuser", null));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any())).thenReturn("jwt_token");

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt_token");
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getRole()).isEqualTo("USER");
    }

    @Test
    void login_InvalidCredentials_ThrowsException() {
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("wrongpassword")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Bad credentials");
        verify(userRepository, never()).findByUsername(any());
    }
}
