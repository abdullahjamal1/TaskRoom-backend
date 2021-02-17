package app.controllers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.net.ssl.SSLEngineResult.Status;

import com.fasterxml.jackson.databind.ser.std.StdArraySerializers.IntArraySerializer;

import org.apache.catalina.connector.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import app.models.entity.Game;
import app.models.projections.GameInfo;
import app.models.projections.GameInfoAbstract;
import app.repositories.GameRepository;
import app.services.GameService;
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

	//=======================================================================
	/**
	 * allow to delete the game if the request is sent by author or admin
	 * @param game_id
	 * @return
	 * 
	 */
	@DeleteMapping("/{game_id}")
	public String deleteGameById(@PathVariable Long game_id,
	@RequestHeader(name="Authorization") String token) {
		
			try {
			
        	gameRepo.deleteById(game_id);
        	
			}
			catch(Exception e) {
				
				return "error occurred while deleting resource" + e.toString();
			}
			return null;
	}
	
	@GetMapping(value="/{game_id}/file",
	produces=MediaType.MULTIPART_MIXED_VALUE)
	public @ResponseBody byte[] getGameFileById(@PathVariable("game_id") Long game_id)
			throws FileNotFoundException, IOException {
		
		return gameService.getGameFile(game_id);
	}
	
	/*
	@return : number of images for a game
	*/
	@GetMapping(value="/{game_id}/images")
	public int getGameImages(@PathVariable("game_id") Long game_id) throws IOException {
		
		return gameService.getGameImages(game_id);
	}
		
	@GetMapping(value="/{game_id}/images/{id}",
	produces = MediaType.IMAGE_JPEG_VALUE)
	public @ResponseBody byte[] getGameImage(@PathVariable("game_id") Long game_id,
	@PathVariable("id") Long imageId) throws IOException {
		
		return gameService.getGameImage(game_id, imageId);
	}
	
	@PostMapping(value="/file")
	@PutMapping(value="/file")
	public IntArraySerializer createNewGame(
								@RequestParam(name="game_id") Long game_id, 
								@RequestPart("images[]") MultipartFile[] images,
								@RequestPart("gameFile") MultipartFile gameFile,
								@RequestHeader(name="Authorization") String token
								) {

									Response response = new Response();

		if(gameService.findGameById(game_id).getUser_id() == jwtUtil.extractUserId(token)){

			gameService.uploadFile( game_id, images, gameFile);
			return Response.SC_OK;
		}
			
		else 
			return Response.SC_UNAUTHORIZED;
	}

	@PostMapping("")
	@PutMapping("")
	public Game postGame(@RequestBody Game game, @RequestHeader(name="Authorization") String token){

		game.setUser_id(jwtUtil.extractUserId(token));
		game.setGame_id(null);
		return gameRepo.save(game);
	}

	
}
