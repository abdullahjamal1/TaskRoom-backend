package app.models.projections;

import java.time.Instant;

public interface GameInfoAbstract {
	
    public Long getGame_id();
	public void setGame_id(Long game_id);
	
	public String getTitle();
	public void setTitle(String name);
	
    public Instant getLast_modified();
    public void setLast_modified(final Instant lastModified);
	
	public String getType();
	public void setType(String type);
	
	public float getRating();
	public void setRating(float rating);

}
