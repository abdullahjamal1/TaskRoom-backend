package app.models.entity;

import lombok.Data;

@Data
public class RegisterationRequest {
    
    private String username;
    private String password;
    private String email;
}
