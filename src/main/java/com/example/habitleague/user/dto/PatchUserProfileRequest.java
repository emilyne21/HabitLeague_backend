package com.example.habitleague.user.dto;

import com.example.habitleague.user.model.AvatarId;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatchUserProfileRequest {
    
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String firstName;
    
    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    private String lastName;
    
    @Size(max = 500, message = "La biografía no puede exceder 500 caracteres")
    private String bio;
    
    @URL(message = "La URL de la foto de perfil debe ser válida")
    private String profilePhotoUrl;
    
    private AvatarId avatarId;
} 