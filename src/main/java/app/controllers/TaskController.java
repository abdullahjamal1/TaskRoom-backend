package app.controllers;

import java.util.List;

import javax.websocket.server.PathParam;

import com.amazonaws.services.xray.model.Http;

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

import app.models.entity.Task;
import app.models.entity.TaskRequest;
import app.repositories.GroupRepository;
import app.repositories.TaskRepository;
import app.services.GroupService;
import app.services.TaskService;
import app.services.UserService;
import app.util.JwtUtil;

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


    @GetMapping("")
    public ResponseEntity<List<Task>> findAllTasks(@RequestParam("groupId") String id,
                                                    @RequestHeader(name="Authorization") String token) {

        if(groupService.isMember(id, token))
            return ResponseEntity.ok(taskRepository.findByGroupId(id));

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTask(@PathVariable("id") String id,
                        @PathParam("groupId") String groupId,
                        @RequestHeader(name="Authorization") String token) {

        if(groupService.isMember(groupId, token))
            return ResponseEntity.ok(taskRepository.findOneBy_idAndGroupId(id, groupId));
      
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }

    // only admins or group admins can create tasks

    @PostMapping("")
    public ResponseEntity<Object> saveTask(
                    @RequestParam("groupId") String groupId, 
                    @RequestBody TaskRequest taskRequest,
                    @RequestHeader(name="Authorization") String token
                    ){
        
        if(groupService.isAdmin(groupId, token)){
    
            Task task = new Task(taskRequest, jwtUtil.extractUsername(token), groupId);
            
            return ResponseEntity.ok(taskService.save(task));
        }
        else return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Only Admins can create new task");

     }

     // only task-author can update a task

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateTask(
                    @PathVariable("id") String id,
                    @RequestBody TaskRequest taskRequest,
                    @RequestHeader(name="Authorization") String token
                    ) {
        
        if (jwtUtil.extractUsername(token) == taskRepository.findAuthorBy_id(id)) {

            Task task = taskRepository.findOneBy_id(id);
            task.setDescription(taskRequest.getDescription());
            return ResponseEntity.ok(taskRepository.save(task));

        } else {
  
            return ResponseEntity.status(403).body(null);
        }
    }

    // task can be deleted by task-author ot group-admin

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteGroup(
        @PathVariable("id") String _id,
    @RequestHeader(name="Authorization") String token) {

        if(jwtUtil.extractUsername(token) == taskRepository.findOneBy_id(_id).getAuthor() 
        || userService.isAdmin(token)){
          
            groupService.deleteGroup(_id);
            return ResponseEntity.ok(null);
        }
        else return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    }
    
}
