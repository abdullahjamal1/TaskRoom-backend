package app.controllers;

import java.io.IOException;
import java.util.List;

import org.apache.catalina.connector.Response;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import app.models.entity.Game;
import app.models.projections.GameInfo;
import app.models.projections.GameInfoAbstract;
import app.repositories.GameRepository;
import app.services.GameService;
import app.services.UserService;
import app.util.JwtUtil;

/**
 * 
 * @author abdullah jamal
 *	
 *	
 *
 */

@RestController
@RequestMapping("/games")
public class GameController {

    @Autowired
    private GameRepository gameRepo;

    @Autowired
    private GameService gameService;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired 
	private UserService userService;

	public static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);
    
	@GetMapping("")
    public List<GameInfoAbstract> displayGameList(
		@RequestParam(value = "filterBy", required = false) String filterBy,
		@RequestParam(value = "value", required = false) String value) {

		return gameService.findGameList(filterBy, value);
    }

	@GetMapping("/{game_id}")
	public GameInfo displayGameById(@PathVariable Long game_id) {
		
		return gameService.findGameById(game_id);
	}
	
	@PutMapping("/{game_id}/rate")
	public void rateGame(
		@PathVariable("game_id") Long game_id,
		@RequestParam("rating") int rating,
		@RequestHeader(name="Authorization") String token
		){

		gameService.rateGame(game_id, rating, jwtUtil.extractUserId(token));
	}

	/**
	 * allows to delete the game if the request is sent by author or admin
	 * @param game_id
	 * @return
	 */
	@DeleteMapping("/{game_id}")
	public ResponseEntity<Object> deleteGameById(@PathVariable Long game_id,
	@RequestHeader(name="Authorization") String token) {
		
		if(gameService.findGameById(game_id).getUser_id().equals(jwtUtil.extractUserId(token)) ||
			userService.isAdmin(token)){
			
			gameService.delete(game_id);
			return ResponseEntity.ok(null);
		}
		else{
			// unAuthorized
			return ResponseEntity.status(HttpStatus.SC_FORBIDDEN).body(null);
		}
	}
	
	/*
	@return : number of images for a game
	*/
	@GetMapping(value="/{game_id}/images")
	public int getGameImages(@PathVariable("game_id") Long game_id) throws IOException {
		
		return gameService.getGameImages(game_id);
	}
	
	@PostMapping(value="/file")
	@PutMapping(value="/file")
	public ResponseEntity<Object> createNewGame(
								@RequestParam(name="game_id") Long game_id, 
								@RequestPart("images[]") MultipartFile[] images,
								@RequestPart("gameFile") MultipartFile gameFile,
								@RequestHeader(name="Authorization") String token
								) {

		if(gameService.findGameById(game_id).getUser_id() == jwtUtil.extractUserId(token)){

			gameService.uploadFile( game_id, images, gameFile);
			return ResponseEntity.status(HttpStatus.SC_OK).body(null);
		}
			
		else 
			return ResponseEntity.status(HttpStatus.SC_FORBIDDEN).body(null);
	}


	@PostMapping("")
	@PutMapping("")
	public Game postGame(@RequestBody Game game, @RequestHeader(name="Authorization") String token){

		game.setUser_id(jwtUtil.extractUserId(token));
		game.setGame_id(null);
		game.setDownloads(null);
		return gameRepo.save(game);
	}


		// @GetMapping(value="/{game_id}/file",
	// produces=MediaType.MULTIPART_MIXED_VALUE)
	// public @ResponseBody byte[] getGameFileById(@PathVariable("game_id") Long game_id)
	// 		throws FileNotFoundException, IOException {
		
	// 	return gameService.getGameFile(game_id);
	// }

		// @GetMapping(value="/{game_id}/images/{id}",
	// produces = MediaType.IMAGE_JPEG_VALUE)
	// public @ResponseBody byte[] getGameImage(@PathVariable("game_id") Long game_id,
	// @PathVariable("id") Long imageId) throws IOException {
		
	// 	return gameService.getGameImage(game_id, imageId);
	// }
	
	
}
