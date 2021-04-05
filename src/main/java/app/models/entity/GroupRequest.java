package app.models.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupRequest {
    
    private String title;
    private String description;
    private String theme;
    private List<String> members;  //usernames of members
    private List<String> admins;
}
