package app.models.entity;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import lombok.Data;

@Entity
@Data
public class Game{

    @GeneratedValue
    @Id
	private Long game_id;
	private String title; 
	
	private Instant creation_time;

    private Instant last_modified;
    
    private Long user_id;
	private Long genre_id;
	
	private String description;
	
	public Game() {
		
	}

    public Game(Long game_id, String title, Instant creation_time, Instant last_modified, Long user_id, Long genre_id,
		 String description) {
		super();
		this.game_id = game_id;
		this.title = title;
		this.creation_time = creation_time;
		this.last_modified = last_modified;
		this.user_id = user_id;
		this.genre_id = genre_id;
		this.description = description;
	}

	@PrePersist
    void createdAt() {

        final Instant i = Instant.now();
        creation_time = i;
        last_modified = i;
    }

    @PreUpdate
    void last_modified() {

        setLast_modified(Instant.now());
    }
 

}
