package app.models.entity;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Document
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Group {

    @Id
    private String _id;
    private String name;
    private String description;
    private String admin; //username
    private Timestamp creationTime;
    private List <String> members;  // list of usernames
    private List<String> admins;
    
    public Group(GroupRequest groupRequest) {
        this.name = groupRequest.getName();
        this.description = groupRequest.getDescription();
        this.admins = groupRequest.getAdmins();
        Date date = new Date();
        this.creationTime = new Timestamp(date.getTime()); 
	}
}
