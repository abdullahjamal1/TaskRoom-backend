package app.controllers;

import java.util.List;

import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
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

import app.models.collections.Group;
import app.models.entity.GroupRequest;
import app.services.GroupService;
import app.services.UserService;
import app.util.JwtUtil;
import io.jsonwebtoken.Claims;

@RestController
@RequestMapping("/groups")
public class GroupController {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserService userService;

    @Autowired
    private GroupService groupService;
    /*
    Allow users to access group resources only if they are members
    */

    @GetMapping("")
    public ResponseEntity<List<Group>> findAll(@RequestHeader(name = "Authorization") String token) {

        if(userService.isAdmin(token))
            return ResponseEntity.ok(groupService.findAll());

        else return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getGroup(@PathVariable("id") final String _id,
    @RequestHeader(name="Authorization") String token) {

        if(groupService.isMember(_id, token)){
            return ResponseEntity.ok(groupService.findOneBy_id(_id));
        }
        else return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not member of group");
    }

    @PostMapping("")
    public ResponseEntity<Object> saveGroup(
                    @RequestBody final GroupRequest group,
                    @RequestHeader(name="Authorization") String token
                    ){
        
        return ResponseEntity.ok(groupService.save(group, token));

     }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateGroup(@PathVariable("id") final String _id,
                    @RequestBody final GroupRequest group,
                    @RequestHeader(name="Authorization") String token
                    ) {

        if (jwtUtil.extractUsername(token).equals(groupService.findAdminBy_id(_id))) {

            return ResponseEntity.ok(groupService.updateGroup(group, _id));

        } else {
            // unauthorized
            return ResponseEntity.status(403).body(null);
        }
    }

    /*
    *   Only groupAdmin can delete group
    */
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteGroup(@PathVariable("id") String _id,
    @RequestHeader(name="Authorization") String token) {

        if(jwtUtil.extractUsername(token).equals(groupService.findAdminBy_id(_id)) || userService.isAdmin(token)){
          
            groupService.deleteGroup(_id);
            return ResponseEntity.ok(null);
        }
        else return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    }

    // join
    @GetMapping("/join")
    public ResponseEntity<Object> joinGroup(@RequestParam("token") String groupToken) {

        // if token sent belongs to the sending user
        
        if( groupService.validateToken(groupToken) ){
            
            groupService.joinGroup(groupToken);
            return ResponseEntity.ok("joined new group :)");
        }
        else 
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("group token is invalid");
    }

    // leave 
    // group admin will be denied
    @GetMapping("/leave/{id}")
    public ResponseEntity<Object> leaveGroup(@PathVariable("id") String _id,
    @RequestHeader(name="Authorization") String token) {

        //admin cannot leave the group
        if( userService.isAdmin( jwtUtil.extractUsername(token) ) ){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Admin cannot leave the group");
        }

        groupService.leaveGroup(_id, token);
        return ResponseEntity.ok("you have left the group :)");
  
    }

}
