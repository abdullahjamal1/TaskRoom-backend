package app.controllers;

import java.util.List;

import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

import app.models.collections.Task;
import app.models.entity.TaskRequest;
import app.repositories.TaskRepository;
import app.services.GroupService;
import app.services.TaskService;
import app.services.UserService;
import app.util.JwtUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private JwtUtil jwtUtil;

    /*
     * @Param String groupId
     * 
     * @Param String sortBy => can take values {updateTime, dueTime, completed,
     * default} default value = "default"
     *
     * @Param String sortOrder => can take value {ASC, DESC} default = "ASC"
     *
     * @Param Integer page, default value = 0
     *
     * @Param Integer perPage default value = 10
     */
    @GetMapping("")
    public Mono<Object> findAllTasks(@RequestHeader(name = "Authorization") String token,
            @RequestParam("groupId") String groupId,
            @RequestParam(name = "sortBy", required = false, defaultValue = "default") String sortBy,
            @RequestParam(name = "sortOrder", required = false, defaultValue = "ASC") String sortOrder,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "perPage", required = false, defaultValue = "10") int perPage) {

        return groupService.isMember(groupId, token).map(isMember -> {

            if (isMember)
                return ResponseEntity.ok(taskRepository.findByGroupId(groupId));

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        });
    }

    @GetMapping("/{id}")
    public Mono<Object> getTask(@PathVariable("id") String id, @RequestHeader(name = "Authorization") String token) {

        return groupService.isMember(id, token).map(isMember -> {

            if (isMember)
                return ResponseEntity.ok(taskRepository.findOneBy_id(id));

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        });
    }

    // only admins or group admins can create tasks

    @PostMapping("")
    public Mono<Object> saveTask(@RequestParam("groupId") String groupId,
            @RequestBody TaskRequest taskRequest, @RequestHeader(name = "Authorization") String token) {

        return groupService.isAdmin(groupId, token).map(isAdmin ->{

            if (isAdmin) {
    
                Task task = new Task(taskRequest, jwtUtil.extractUsername(token), groupId);
    
                return ResponseEntity.ok(taskService.save(task));
            } else
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Only Admins can create new task");
        });

    }

    // only task-author can update a task

    @PutMapping("/{id}")
    public ResponseEntity<Mono<Task>> updateTask(@PathVariable("id") String id, @RequestBody TaskRequest taskRequest,
            @RequestHeader(name = "Authorization") String token) {

        if (jwtUtil.extractUsername(token).equals(taskService.findAuthorBy_id(id))) {

            return ResponseEntity.ok(taskService.updateTask(taskRequest, id));

        } else {

            return ResponseEntity.status(403).body(null);
        }
    }

    // task can be deleted by task-author ot group-admin

    @DeleteMapping("/{id}")
    public Mono<Object> deleteGroup(@PathVariable("id") String _id,
            @RequestHeader(name = "Authorization") String token) {

        return userService.isAdmin(token).map(isAdmin ->{

            if (jwtUtil.extractUsername(token).equals(taskRepository.findOneBy_id(_id).map(t -> t.getAuthor()))
                    || isAdmin) {
    
                taskService.deleteTask(_id);
                return ResponseEntity.ok(null);
            } else
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        });
    }

}
