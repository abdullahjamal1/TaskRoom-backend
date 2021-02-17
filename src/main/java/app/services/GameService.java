package app.services;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
    
    public List<GameInfoAbstract> findGameList(String filterBy, String value){

        // if(filterBy == "genre"){

        //     return gameRepo.findAllGameByGenre(value);

        // }else if(filterBy == "rating"){

        //     return gameRepo.gameListWithRatingAbove(value);

        // }else if(filterBy == "date"){

        //     return gameRepo.gameFilterByLastUpdate(value);

        // }
        
        return gameRepo.findAllGame();
    }

    public GameInfo findGameById(Long game_id) {
    	
    	return gameRepo.findOneByGameId(game_id);
    }

	public Game uploadFile(Long game_id, MultipartFile[] images, MultipartFile gameFile) {


        // if user_id is game-author allow else not authorized

        String gameDirectory = config.getUserRoot() + File.separator + "games" + File.separator;
        String imageDirectory = gameDirectory + game_id +  File.separator + "images" + File.separator;
        String  gamePath = gameDirectory + game_id +  File.separator + "game" + File.separator + game_id;

        // upload game file
        this.amazonS3ClientService.uploadFileToS3Bucket(gameFile, gamePath + gameFile.getOriginalFilename(), true);

        //upload images
        for(int i = 1; i <= images.length; i++){

            this.amazonS3ClientService.uploadFileToS3Bucket(images[i], imageDirectory + i + ".jpg" , true);
        }
        return null;
	}

	public void rateGame(Long game_id, int rating, Long user_id) {
  
		Ratings rate = new Ratings();
		rate.setGame_id(game_id);
		rate.setUser_id(user_id);
		rate.setRating(rating);
        rateRepo.save(rate);
	}

}
