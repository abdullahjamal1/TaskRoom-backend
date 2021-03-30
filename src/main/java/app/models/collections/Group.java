package app.models.collections;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import app.models.entity.GroupRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Document
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Group {

    @Id
    private String _id;
    private String title;
    private String description;
    private String admin; //username
    private String theme;
    private Date creationTime;
    private List <String> members;  // list of usernames
    private List <String> admins;
    
    public Group(GroupRequest groupRequest, String admin) {
        this.title = groupRequest.getTitle();
        this.description = groupRequest.getDescription();
        this.admins = groupRequest.getAdmins();
        this.theme = groupRequest.getTheme();
        this.admin = admin;
        List <String> array = new ArrayList<String>();
        array.add(admin);
        this.members = array;
        this.admins = array;
        this.creationTime = new Date();
	}


}
