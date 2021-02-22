package app.models.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class Genre {
	
	@GeneratedValue
	@Id
	private Long genre_id;
	
	private String type;

}
