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
public class Comment {

    @Id
    private String _id;
    private String Author;
    private String comment;
    private Timestamp creationTime;
    private String taskId;
    private String parentId;

    public Comment(String taskId, String comment, String parentId, String username) {
        this.taskId = taskId;
        this.comment = comment;
        this.parentId = parentId;
        this.Author = username;
        Date date = new Date();
        this.creationTime = new Timestamp(date.getTime());   
    }
}
