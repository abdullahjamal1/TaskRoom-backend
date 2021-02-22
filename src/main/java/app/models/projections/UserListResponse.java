package app.models.projections;

import java.time.Instant;

public interface UserListResponse {

    public Long getId();
    public void setId(final Long id);

    public Instant getCreation_time();
    public void setCreation_time(final Instant creationTime);
    
    public String getUsername();
    public void setUsername(final String name);

    public String getFirst_name();
    public void setFirst_name(final String First_name);

    public String getLast_name();
    public void setLast_name(final String Last_name);

    public Long getUploads();
    public void setUploads(final Long uploads);

}
