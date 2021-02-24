package app.models.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    private String _id;
    private String username;
    private String password;
    private String email;
    private String token;
    private String role;
    private String name;
    private List<String> groups;

    public boolean isAdmin(){
        return this.getRole().equals("ADMIN") ? true : false;
    }

}
