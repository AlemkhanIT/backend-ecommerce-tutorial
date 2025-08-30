package com.ecommerce.user.dto;

import com.ecommerce.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent {
    private String eventType;
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private User.Role role;
    private LocalDateTime timestamp;
    
    public static UserEvent userRegistered(User user) {
        UserEvent event = new UserEvent();
        event.setEventType("USER_REGISTERED");
        event.setUserId(user.getId());
        event.setEmail(user.getEmail());
        event.setFirstName(user.getFirstName());
        event.setLastName(user.getLastName());
        event.setRole(user.getRole());
        event.setTimestamp(LocalDateTime.now());
        return event;
    }
    
    public static UserEvent userEmailConfirmed(User user) {
        UserEvent event = new UserEvent();
        event.setEventType("USER_EMAIL_CONFIRMED");
        event.setUserId(user.getId());
        event.setEmail(user.getEmail());
        event.setFirstName(user.getFirstName());
        event.setLastName(user.getLastName());
        event.setRole(user.getRole());
        event.setTimestamp(LocalDateTime.now());
        return event;
    }
}
