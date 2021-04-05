package app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.models.collections.Comment;
import app.models.entity.CommentRequest;
import app.repositories.CommentRepository;
import app.services.CommentService;
import app.services.TaskService;
import app.services.UserService;
import app.util.JwtUtil;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/comments")
public class CommentController {

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private UserService userService;

	@Autowired
	private TaskService taskService;

	@Autowired
	private CommentService commentService;

	@Autowired
	private CommentRepository commentRepo;

	@GetMapping("")
	public Mono<?> findAllComments(@RequestParam("taskId") String taskId, @RequestParam("parentId") String parentId,
			@RequestHeader("Authorization") String token) {

		return taskService.isMember(taskId, token).map(isMember -> {
			if (isMember)
				return ResponseEntity.ok(commentRepo.findByTaskIdAndParentId(taskId, parentId));
			else
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		});

	}

	@PostMapping("")
	public Mono<Object> postComment(@RequestParam("taskId") String taskId,
			@RequestBody Mono<CommentRequest> commentRequest, @RequestParam("parentId") String parentId,
			@RequestHeader("Authorization") String token) {

		return taskService.isMember(taskId, token).map(isMember -> {

			if (isMember) {

				Comment comment = new Comment(taskId, commentRequest, parentId, jwtUtil.extractUsername(token));
				return ResponseEntity.ok(commentRepo.save(comment));
			}
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

		});
	}
	// ==================================================================

	/**
	 * @RootAdmin
	 * @AuthorOfComment
	 * @return
	 */

	@DeleteMapping("/{id}")
	public Mono<Object> deleteComment(@PathVariable("id") String commentId,
			@RequestHeader(name = "Authorization") String token) {

		return userService.isAdmin(token).map(isAdmin -> {

			if (isAdmin || commentService.findAuthorBy_id(commentId).equals(jwtUtil.extractUsername(token))) {

				commentRepo.deleteBy_id(commentId);
				return ResponseEntity.ok(null);
			}

			else
				return ResponseEntity.status(403).body("un-authorized to delete the resource");
		});

	}

}
