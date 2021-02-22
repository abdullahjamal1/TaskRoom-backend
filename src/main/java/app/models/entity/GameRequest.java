package app.models.entity;

import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

public class GameRequest implements Serializable{

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    
    
	private String title; 
    
	private Long genre_id;
	
	private String description;
    
    MultipartFile gameFile;
    MultipartFile images[];

	public MultipartFile getGameFile() {
		return this.gameFile;
	}

	public void setGameFile(MultipartFile gameFile) {
		this.gameFile = gameFile;
	}

	public MultipartFile[] getImages() {
		return this.images;
	}

	public void setImages(MultipartFile images[]) {
		this.images = images;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Long getGenre_id() {
		return this.genre_id;
	}

	public void setGenre_id(Long genre_id) {
		this.genre_id = genre_id;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


    GameRequest(){

    }


    /**
     * @param gameFile
     * @param images
     * @param title
     * @param genre_id
     * @param description
     */
    public GameRequest(MultipartFile gameFile, MultipartFile[] images, String title, Long genre_id,
            String description) {
        this.gameFile = gameFile;
        this.images = images;
        this.title = title;
        this.genre_id = genre_id;
        this.description = description;
    }

}
