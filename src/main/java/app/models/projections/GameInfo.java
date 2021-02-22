package app.models.projections;

import java.time.Instant;

public interface GameInfo extends GameInfoAbstract{
	
	/*
	* 
	*  from game entity
	*/
	
	public Long getGame_id();
	public void setGame_id(Long game_id);

	public Long getUser_id();
	public void setUser_id(Long user_id);



	public Long getGenre_id();
	public void setGenre_id(Long genre_id);

	public Instant getCreation_time();
    public void setCreation_time(final Instant creationTime);

	public String getDescription();
	public void setDescription(String description);

	public String getTitle();
	public void setTitle(String name);

	public String getType();
	public void setType(String type);
	
	public float getRating();
	public void setRating(float rating);
	
	
	/*
	*  from genre User
	*/
    public String getUsername();
    public void setUsername(final String name);

	// custom

	public Long getRatedBy();
	public void setRatedBy(Long ratedBy);

	public Long getDownloads();
	public void setDownloads(Long downloads);	

}
