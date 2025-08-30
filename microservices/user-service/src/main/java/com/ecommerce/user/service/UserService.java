package com.ecommerce.user.service;

import com.ecommerce.user.dto.*;
import com.ecommerce.user.exception.ResourceNotFoundException;
import com.ecommerce.user.exception.UserAlreadyExistsException;
import com.ecommerce.user.mapper.UserMapper;
import com.ecommerce.user.model.User;
import com.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final KafkaTemplate<String, UserEvent> kafkaTemplate;
    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Transactional
    public UserDTO registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(User.Role.USER);
        user.setEmailConfirmed(false);
        user.setConfirmationCode(generateConfirmationCode());
        user.setConfirmationCodeExpiry(LocalDateTime.now().plusHours(24));

        User savedUser = userRepository.save(user);

        // Send confirmation email
        emailService.sendConfirmationEmail(savedUser);

        // Publish user registered event
        UserEvent event = UserEvent.userRegistered(savedUser);
        kafkaTemplate.send("user-events", event);

        log.info("User registered successfully: {}", savedUser.getEmail());
        return userMapper.toDTO(savedUser);
    }

    public String login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = (User) authentication.getPrincipal();
        return jwtService.generateTokenWithRole(user.getEmail(), user.getRole().name());
    }

    @Transactional
    public void confirmEmail(EmailConfirmationRequest request) {
        User user = userRepository.findByValidConfirmationCode(request.getConfirmationCode())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired confirmation code"));

        if (!user.getEmail().equals(request.getEmail())) {
            throw new IllegalArgumentException("Email does not match confirmation code");
        }

        user.setEmailConfirmed(true);
        user.setConfirmationCode(null);
        user.setConfirmationCodeExpiry(null);
        userRepository.save(user);

        // Publish email confirmed event
        UserEvent event = UserEvent.userEmailConfirmed(user);
        kafkaTemplate.send("user-events", event);

        log.info("Email confirmed for user: {}", user.getEmail());
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed for user: {}", user.getEmail());
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return userMapper.toDTO(user);
    }

    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return userMapper.toDTO(user);
    }

    @Transactional
    public void resendConfirmationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isEmailConfirmed()) {
            throw new IllegalStateException("Email is already confirmed");
        }

        user.setConfirmationCode(generateConfirmationCode());
        user.setConfirmationCodeExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        emailService.sendConfirmationEmail(user);
        log.info("Confirmation email resent for user: {}", user.getEmail());
    }

    private String generateConfirmationCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
