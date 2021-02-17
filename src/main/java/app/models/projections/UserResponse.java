package app.models.projections;

import java.time.Instant;

public interface UserResponse {

    public Long getId();
    public void setId(final Long id);

    public Instant getCreation_time();
    public void setCreation_time(final Instant creationTime);
    
    public String getUser_name();
    public void setUser_name(final String name);

    public String getRole();
    public void setRole(final String role);

    public String getFirst_name();
    public void setFirst_name(final String firstName);

    public String getLast_name();
    public void setLast_name(final String lastName);

    // public String getLastLogin();
    // public void setLastLogin(final String lastLogin);
}
