package app.models.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import reactor.core.publisher.Mono;

@Data

@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CommentRequest {

    private String comment;
}
