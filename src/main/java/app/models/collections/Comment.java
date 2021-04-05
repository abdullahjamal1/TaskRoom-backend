package app.models.collections;

import java.util.Date;

import app.models.entity.CommentRequest;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import reactor.core.publisher.Mono;

@Document
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Comment {

    @Id
    private String _id;
    private String Author;
    private Mono<Object> comment;
    private Date creationTime;
    private String taskId;
    private String parentId;

    public Comment(String taskId, Mono<CommentRequest> comment, String parentId, String username) {
        this.taskId = taskId;
        this.comment = comment.map(c -> c.getComment());
        this.parentId = parentId;
        this.Author = username;
        this.creationTime = new Date();   
    }
}
