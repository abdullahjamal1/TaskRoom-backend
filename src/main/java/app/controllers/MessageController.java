package app.controllers;

import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import app.models.entity.Message;
import app.models.projections.MessageInfo;
import app.services.MessageService;
import app.util.JwtUtil;

/**
 * 
 * GET : /game/{game_id}/messages
 * 
 */

@RestController
@RequestMapping("/comments")
public class MessageController{
	
	@Autowired
	private MessageService messageService;	

	@Autowired
	private JwtUtil jwtUtil;
	
	@GetMapping("/game/{game_id}")
	public List<MessageInfo> findAllMessages(@PathVariable("game_id") Long game_id,	
				@RequestParam(value="parent_id") Long parent_id) {
		
			return messageService.findAllMessages(game_id, parent_id);			
	}
	
	
	/**
	 * vote = 1 for upvote
	 * vote = 0 for downVote
	 * 
	 * @param msg_id
	 * @param vote
	 * @return
	 */
	
	@PutMapping("/{msg_id}/vote")
	public void voteMessage(@PathVariable("msg_id") Long msg_id,
	@RequestParam boolean vote,
	@RequestHeader(name="Authorization") String token
	) {

		messageService.voteMessage(msg_id, vote, jwtUtil.extractUserId(token));
	}
	
	
	@PostMapping("/game/{game_id}")
	public Message postMessage(@PathVariable("game_id") Long game_id,
	@RequestBody Message message,
	@RequestParam(value="parent_id") Long parent_id,
	@RequestHeader(name="Authorization") String token
	) { 
		
		return messageService.save(message, game_id, parent_id, jwtUtil.extractUserId(token));
	}
	//==================================================================
	
	/**
	 * @Admin
	 * @AuthorOfMessage
	 * 
	 * @param msg_id
	 * @return
	 */
	
	@DeleteMapping("/{msg_id}")
	public ResponseEntity<Object> deleteMessage(@PathVariable("msg_id") Long msg_id
									,@RequestHeader(name="Authorization") String token) {
		
		if(messageService.isMessageAuthorOrAdmin(msg_id, jwtUtil.extractUserId(token))){

			messageService.deleteMessageById(msg_id, jwtUtil.extractUserId(token));
			return ResponseEntity.ok(null);
		}

		else return ResponseEntity.status(403).body("un-authorized to delete the resource");
	}
	
}
