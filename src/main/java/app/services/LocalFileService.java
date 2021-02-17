package app.services;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import app.configs.ApplicationConfig;

@Service
@EnableAsync
public class LocalFileService {
    
    @Autowired
    private ApplicationConfig config;

    @Autowired
    private UserService userService;

    private static final Logger LOGGER = LoggerFactory.getLogger(AwsS3Service.class);
    
    @Async
    public void saveFileLocaly(Long game_id, MultipartFile[] images, MultipartFile file, Long user_id){
        
        String gameDirectory = config.getUserRoot() + File.separator + "games" + File.separator;
        String saveDirectory = gameDirectory + game_id +  File.separator + "images" + File.separator;
        String  filePath = gameDirectory + game_id +  File.separator + "game" + File.separator;
      
        if (images.length != 0) {
            LOGGER.info("img is not null");
        	
            try {        	
                userService.createSaveDirectory(saveDirectory);

                for(int i = 1; i <= images.length; i++){
    
                    final byte[] bytes = images[i].getBytes();
    
                    final ByteArrayInputStream imageInputStream = new ByteArrayInputStream(bytes);
                    final BufferedImage image = ImageIO.read(imageInputStream);

                    // for thumbnail
                    // final BufferedImage thumbnail = Scalr.resize(image, 200);
                    final BufferedImage thumbnail = image;
    
                    LOGGER.info(saveDirectory + i);
                    final File thumbnailOut = new File(saveDirectory + i +".jpg");
                    ImageIO.write(thumbnail, "png", thumbnailOut);
                }
                LOGGER.info("Image Saved::: {}");
                
            } catch (final Exception e) {
            	
                LOGGER.info("Error Uploading File", e);
            }
        }
        if (!file.isEmpty()) {
            //filter for checking file extewnsion
            // if(file.getContentType().equalsIgnoreCase("image/jpg") || file.getContentType().equalsIgnoreCase("image/jpeg")){
        	
	            try {
	                byte[] bytes = file.getBytes();
	                userService.createSaveDirectory(filePath);
	                
	                BufferedOutputStream stream =
	                        new BufferedOutputStream(new FileOutputStream(new File(filePath + game_id)));
	                
	                stream.write(bytes);
	                stream.close();

	            }catch (Exception e) {
	            	
	                LOGGER.info("Error Uploading File", e);
	            }
                LOGGER.info("game file saved");
            // }
        }
        
    }

    public int getGameImages(Long game_id) throws IOException {
        
        String gameDirectory = config.getUserRoot() + File.separator + "games" + File.separator;
        final String gameImageDir = gameDirectory + game_id + File.separator + "images" + File.separator;
        
        return new File(gameImageDir).list().length;

        }

    public byte[] getGameImage(Long game_id, Long imageId) throws IOException {

        String gameDirectory = config.getUserRoot() + File.separator + "games" + File.separator;
        final String gameImageDir = gameDirectory + game_id + File.separator + "images" + File.separator;
        return getGameFile(gameImageDir + imageId + ".jpg");

    }

	public byte[] getGameFile(Long game_id) throws FileNotFoundException, IOException {
        
        String gameDirectory = config.getUserRoot() + File.separator + "games" + File.separator;
        final String gameFile = gameDirectory + game_id + File.separator + "game" + File.separator + game_id;
        
        return getGameFile(gameFile + ".jpg");
	}
	
	public byte[] getGameFile(String location) throws FileNotFoundException, IOException {
        
        // LOGGER.info(location);
        try {
            if (new File(location).exists()) {
        	
                return IOUtils.toByteArray(new FileInputStream(location));
                
            } 
        } catch (FileNotFoundException f) {
            //TODO: handle exception
            LOGGER.info("file not found" + f);
        }
        catch(IOException io){
            LOGGER.info("io" + io);
        }

        return null;

	}
}
