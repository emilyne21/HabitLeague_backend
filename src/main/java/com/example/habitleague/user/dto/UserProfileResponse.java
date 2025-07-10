package com.example.habitleague.user.dto;

import com.example.habitleague.user.model.AvatarId;
import com.example.habitleague.user.model.User;
import com.example.habitleague.user.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String bio;
    private String profilePhotoUrl;
    private AvatarId avatarId;
    private UserRole role;
    private LocalDateTime createdAt;

    public static UserProfileResponse fromUser(User user) {
        return new UserProfileResponse(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getBio(),
            user.getProfilePhotoUrl(),
            user.getAvatarId(),
            user.getRole(),
            user.getCreatedAt()
        );
    }
} 