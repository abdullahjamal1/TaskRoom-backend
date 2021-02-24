package app.models.entity;

import java.sql.Timestamp;
import java.util.Date;

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
public class Task {

    @Id
    private String _id;
    private String groupId;
    private String description;
    private Timestamp creationTime;
    private String author;

	public Task(TaskRequest taskRequest, String username, String groupId) {
        this.description = taskRequest.getDescription();
        this.author = username;
        this.groupId = groupId;
        Date date = new Date();
        this.creationTime = new Timestamp(date.getTime()); 
	}
    
}
