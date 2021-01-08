package app.controllers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import app.models.entity.Game;
import app.models.entity.User;
import app.repositories.GameRepository;
import app.repositories.UserRepository;
import app.services.GameService;
import app.services.UserService;
import app.models.projections.GameInfo;
import app.models.projections.GameInfoAbstract;
import io.swagger.annotations.ApiOperation;

/**
 * 
 * @author abdullah jamal
 *	
 *	ERROR change DB game to rating should be 0 to many
 *
 * 
 * GET : /game/list
 * GET : /game/{game_id}/detail
 * DELETE: /game/{game_id}/delete  @NotTested @Author @Admin
 * GET : /game/{game_id}/file
 * GET : /game/{game_id}/pic
 * POST : /game              // add redirecting method
 *
 */

@RestController
public class GameController {

    @Autowired
    private GameRepository gameRepo;

    @Autowired
    private GameService gameService;
    
    /**
     * 
     * return list of all games 
     * 
     * @return
     */
	@GetMapping("/game/list")
    public List<GameInfoAbstract> displayGameList() {

		return gameService.findGameList();
    }
	
	/**
     * returns deatiled information of a particular game
	 * @param game_id
	 * @return
	 */
	/*
	 *  in return JSON specify whether user is admin or not
	 */

	@GetMapping("/game/{game_id}/detail")
	@ApiOperation(value = "game id" ,notes= "returns detailed info about a game", response = GameInfo.class)
	public GameInfo displayGameById(@PathVariable Long game_id) {
		
		return gameService.findGameById(game_id);
	}
	
	/**
	 * @Admin
	 * @param game_id
	 * @return
	 */
	@DeleteMapping("/game/{game_id}/delete")
	public String deleteGameById(@PathVariable Long game_id) {
		
		/*
		 *  implement error throw when resource does not exist
		 */
		if(gameService.isAuthorOrAdmin(game_id)) {
			
			try {
			
        	gameRepo.deleteById(game_id);
        	
			}
			catch(Exception e) {
				
				return "error occurred while deleting resource";
			}
        	
        	return "game successfully deleted";
		}
		else {
			
            return "user/premission-denied";
		}	
	}
	
	@GetMapping(value = "/game/{game_id}/file", produces = MediaType.TEXT_HTML_VALUE)
	public @ResponseBody byte[] getGameFileById(@PathVariable("game_id") Long game_id) throws FileNotFoundException, IOException {
		
		return gameService.getGameFileById(game_id);
	}
	
	@GetMapping(value = "/game/{game_id}/pic", produces = MediaType.IMAGE_JPEG_VALUE)
	@ApiOperation(value = "game id" ,notes=" ")
	public @ResponseBody byte[] getGamePicById(@PathVariable("game_id") Long game_id) throws IOException {
		
		return gameService.getGamePicById(game_id);
	}
	
	// game_id should be updated along with the one assigned from database
	// post methos should redirect user to the link where game is posted ... ie gmae/{game_id}/detail
	@PostMapping("/game")
	public String createNewGame(
								@RequestPart("file") final MultipartFile file, 
								@RequestPart("pic") final MultipartFile pic,
								@RequestPart Game game
								) {
	
		
		return gameService.createNewGame(file, pic, game);
	}

	
}
