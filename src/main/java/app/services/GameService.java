package app.services;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipFile;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import app.configs.ApplicationConfig;
import app.controllers.AuthController;
import app.models.entity.Game;
import app.models.entity.Ratings;
import app.models.projections.GameInfo;
import app.models.projections.GameInfoAbstract;
import app.repositories.GameRepository;
import app.repositories.RateRepository;

@Service
@EnableAsync
public class GameService {

    @Autowired
    private GameRepository gameRepo;

    @Autowired
    private RateRepository rateRepo;

    @Autowired
    private AmazonS3ClientService amazonS3ClientService;

    @Autowired
    private ApplicationConfig config;

    public static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    public List<GameInfoAbstract> findGameList(String filterBy, String value) {

        // if(filterBy == "genre"){

        // return gameRepo.findAllGameByGenre(value);

        // }else if(filterBy == "rating"){

        // return gameRepo.gameListWithRatingAbove(value);

        // }else if(filterBy == "date"){

        // return gameRepo.gameFilterByLastUpdate(value);

        // }

        return gameRepo.findAllGame();
    }

    public GameInfo findGameById(Long game_id) {

        return gameRepo.findOneByGameId(game_id);
    }

    // client will upload game in a zip file
    @Async
    public void uploadFile(Long game_id, MultipartFile[] images, MultipartFile gameFile) {

        // if user_id is game-author allow else not authorized

        String gameDirectory = "games/";
        String imageDirectory = gameDirectory + game_id + "/images/";
        String gamePath = gameDirectory + game_id + "/game/";

        this.amazonS3ClientService.uploadFileToS3Bucket(gameFile, gamePath + game_id + ".zip", true);

        // upload images
        for (int i = 0; i < images.length; i++) {

            this.amazonS3ClientService.uploadFileToS3Bucket(images[i], imageDirectory + (i+1) + ".jpg", true);
        }
    }

    public void rateGame(Long game_id, int rating, Long user_id) {

        Ratings rate = new Ratings();
        rate.setGame_id(game_id);
        rate.setUser_id(user_id);
        rate.setRating(rating);
        rateRepo.save(rate);
    }

        // returns count of images associated with a game
	public int getGameImages(Long game_id) {

        String gameDirectory ="games/";
        String imageDirectory = gameDirectory + game_id +  "/images";
		return this.amazonS3ClientService.getFileListFromFolder(imageDirectory).size();
	}

    @Async
	public void delete(Long game_id) {

        gameRepo.deleteById(game_id);

        String gameDirectory = "games/" + game_id;
        this.amazonS3ClientService.deleteFileFromS3Bucket(gameDirectory);
	}

    // public byte[] getGameFile(Long game_id) throws IOException {
		
    //     String gameDirectory = config.getUserRoot() + "/" + "games" + "/";
    //     String  gamePath = gameDirectory + game_id +  "/" + "game" + "/" + game_id;

    //     S3Object s3Object = this.amazonS3ClientService.getFileFromS3Bucket(gamePath);
    //     S3ObjectInputStream inputStream = s3Object.getObjectContent();

    //     byte[] bytes = StreamUtils.copyToByteArray(inputStream);
    //     // String contentType = s3Object.getObjectMetadata().getContentType();

    //     return bytes;
	// }

	// public byte[] getGameImage(Long game_id, Long imageId) throws IOException {

    //     String gameDirectory = config.getUserRoot() + "/" + "games" + "/";
    //     String imageDirectory = gameDirectory + game_id +  "/" + "images" + "/";
        
    //     S3Object s3Object = this.amazonS3ClientService.getFileFromS3Bucket(imageDirectory + imageId);
    //     S3ObjectInputStream inputStream = s3Object.getObjectContent();

    //     byte[] bytes = StreamUtils.copyToByteArray(inputStream);
    //     // String contentType = s3Object.getObjectMetadata().getContentType();

    //     return bytes;
	// }



}
