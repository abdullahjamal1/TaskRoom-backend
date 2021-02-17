package app.models.entity;

import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class User extends AbstractEntity{
	
	// implements Serializable 

   // private static final long serialVersionUID = -4512071323586705398L;

    private static final int MAX_CHARS = 100;

    private static final int MIN_CHARS = 3;

    @NotNull
    @Size(min = MIN_CHARS, max = MAX_CHARS, message = "Username must be at least 3 characters.")
    private String userName;

    @NotNull
    @Size(min = MIN_CHARS, max = MAX_CHARS, message = "Password must be at least 3 characters.")
    private String password;

    @Transient
    private String confirmPassword;

    @Email(message = "Email address is not valid.")
    @NotNull
    private String email;

    private String token;

    private String role = "ROLE_USER";

    private String firstName;

    private String lastName;

    private String lastLogin;
    
    public User(String userName, String password){
        this.userName = userName;
        this.password = password;
    }

    public User(){
        
    }

    public String getUserName() {

        return userName;
    }

    public void setUserName(final String name) {

        this.userName = name;
    }


    public String getRole() {

        return role;
    }

    public void setRole(final String role) {

        this.role = role;
    }

    public String getFirstName() {

        return firstName;
    }

    public void setFirstName(final String firstName) {

        this.firstName = firstName;
    }

    public String getLastName() {

        return lastName;
    }

    public void setLastName(final String lastName) {

        this.lastName = lastName;
    }


    public String getLastLogin() {

        return lastLogin;
    }

    public void setLastLogin(final String lastLogin) {

        this.lastLogin = lastLogin;
    }

    public String getPassword() {

        return password;
    }

    public void setPassword(final String password) {

        this.password = password;
    }

    public String getConfirmPassword() {

        return confirmPassword;
    }

    public void setConfirmPassword(final String confirmPassword) {

        this.confirmPassword = confirmPassword;
    }

    public String getToken() {

        return token;
    }

    public void setToken(final String token) {

        this.token = token;
    }

    public String getEmail() {

        return email;
    }

    public void setEmail(final String email) {

        this.email = email;
    }

    // public boolean isMatchingPasswords() {

    //     return this.password.equals(this.confirmPassword);
    // }
  
}