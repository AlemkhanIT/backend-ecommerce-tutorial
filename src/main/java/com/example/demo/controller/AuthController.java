package com.example.demo.controller;

import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.dto.EmailConfirmationRequest;
import com.example.demo.dto.LoginRequest;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.User;
import com.example.demo.service.CartService;
import com.example.demo.service.JwtService;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final CartService cartService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
        final UserDetails userDetails = userService.getUserByEmail(loginRequest.getEmail());
        final String jwt = jwtService.generateToken(userDetails);
        return ResponseEntity.ok(jwt);
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody User user){
        User registeredUser = userService.registerUser(user);
        cartService.createCartForUser(registeredUser.getId());
        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        userService.changePassword(email, request);
        return ResponseEntity.ok().body("Password changed");
    }

    @PostMapping("/confirm-email")
    public ResponseEntity<?> confirmEmail(@RequestBody EmailConfirmationRequest request){
        try{
            userService.confirmEmail(request.getEmail(), request.getConfirmationCode());
            return ResponseEntity.ok().body("Email confirmed successfully");
        }catch (BadCredentialsException e){
            return ResponseEntity.badRequest().body("Invalid confirmation code");
        }
        catch (ResourceNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }

    //update
    @GetMapping("/user/role")
    public ResponseEntity<String> getUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userService.getUserByEmail(email);

        if (user != null) {
            String role = String.valueOf(user.getRole());
            return ResponseEntity.ok(role);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/user/confirmation")
    public ResponseEntity<String> getUserConfirmation() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userService.getUserByEmail(email);

        if (user != null) {
            String confirmed = String.valueOf(user.isEmailConfirmation());
            return ResponseEntity.ok(confirmed);
        }
        return ResponseEntity.notFound().build();
    }

    //update
    @GetMapping("/user/{id}")
    public ResponseEntity<String> getUserEmailById(@PathVariable Long id) {
        User user = userService.getUserById(id); // Ensure this method exists in your UserService
        if (user != null) {
            return ResponseEntity.ok(user.getEmail());
        }
        return ResponseEntity.notFound().build();
    }
}