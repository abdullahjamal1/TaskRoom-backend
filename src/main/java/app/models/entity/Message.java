package app.models.entity;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;

import lombok.Data;

@Entity
@Data
public class Message {
	
	@GeneratedValue
	@Id
	private Long msg_id;
	private Long user_id;
	private String description;
    private Instant creation_time;
    private Long game_id;
	private Long parent_id;
    
    public Message() {
    	
    }
 
    public Message(Long msg_id, Long user_id, String description, Instant creationTime, Long game_id) {
		super();
		this.msg_id = msg_id;
		this.user_id = user_id;
		this.description = description;
		this.creation_time = creationTime;
		this.game_id = game_id;

	}

	@PrePersist
    void createdAt() {

        final Instant i = Instant.now();
        creation_time = i;
    }


}
