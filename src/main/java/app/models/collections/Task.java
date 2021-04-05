package app.models.collections;

import java.sql.Timestamp;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import app.models.entity.TaskRequest;
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
    private String title;
    private Date updateTime;
    private Date dueTime;
    private String author;
    private boolean isCompleted;

	public Task(TaskRequest taskRequest, String username, String groupId) {
        this.description = taskRequest.getDescription();
        this.title = taskRequest.getTitle();
        this.dueTime = taskRequest.getDueTime();
        this.author = username;
        this.groupId = groupId;
        this.updateTime = new Date(); 
        this.isCompleted = taskRequest.isCompleted();
	}

    public void setDescription(String decription){
        this.description = description;  
    }

    public String getShortDescription(int letters){
        return this.description.substring(0, Math.min(this.description.length(), letters));
    }

	public Task(TaskRequest taskRequest, String id) {
        this.description = taskRequest.getDescription();
        this.title = taskRequest.getTitle();
        this.dueTime = taskRequest.getDueTime();
        this.isCompleted = taskRequest.isCompleted();
        this._id = id;
        this.updateTime = new Date();
	}
    
}
