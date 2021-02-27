package app.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import app.models.collections.Group;
import app.models.entity.GroupRequest;
import app.models.collections.User;
import app.repositories.GroupRepository;
import app.repositories.UserRepository;
import app.util.JwtUtil;
import io.jsonwebtoken.Claims;

@EnableAsync
@Service
public class GroupService {

    @Autowired
    private MailService mailService;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired 
    private JwtUtil jwtUtil;

    @Autowired
    private TaskService taskService;

    // throw error in save and delete if an un-existing user is added

    // switch username in JWT groupToken to email/userID

	public Group save(GroupRequest groupRequest, String token) {

        Group group = new Group(groupRequest, jwtUtil.extractUsername(token));
        group = groupRepository.save(group);

        sendInviteToGroup(group, groupRequest);

        // make admin join his group
        User user = userRepository.findOneByUsername(jwtUtil.extractUsername(token));

        List<String> groups = user.getGroups();
        groups.add(group.get_id());
        user.setGroups(groups);
        userRepository.save(user);
        
        return group;
	}
    
    @Async
    public void sendInviteToGroup(Group group, GroupRequest groupRequest){
        
        for(String username : groupRequest.getMembers()){
            
            if(username != group.getAdmin())
            sendInviteToOne(group, username);
        }      
    }

    public String getGroupToken(String groupId, String email){
        
        // creating a jwt token for invite
        // jti stands for json token id 
        Map<String, Object> claims = new HashMap<>();
        claims.put("jti", groupId);
        return jwtUtil.createToken(claims, email);
    }
    
    @Async
    public void sendInviteToOne(Group group, String username){

        User user = userRepository.findOneByUsername(username);

        String inviteToken = getGroupToken(group.get_id(), user.getEmail());

        mailService.sendInvite(user.getEmail(), username, group.getTitle(), group.getAdmin(), inviteToken);

    }

	public Group updateGroup(GroupRequest groupRequest, String _id) {
		
        Group group = groupRepository.findOneBy_id(_id);
        group.setDescription(groupRequest.getDescription());
        group.setTitle(groupRequest.getTitle());
        group.setAdmins(groupRequest.getAdmins());
        group.setTheme(groupRequest.getTheme());
        
        // if users are not there in old group then send invite
        for(String username : groupRequest.getMembers()){
            
            if(!group.getMembers().contains(username)){
                sendInviteToOne(group, username);
            }
        }
        
        // if users were not there in new group but were there in old group
        // remove them 
        for(String username: group.getMembers()){
            
            if(!groupRequest.getMembers().contains(username)){

                User user = userRepository.findOneByUsername(username);
                List<String> groups = user.getGroups();
                groups.remove(_id);
                user.setGroups(groups);
                userRepository.save(user);
            }           
        }
        // group.setMembers( groupRequest.getMembers());

        return groupRepository.save(group);
	}

    @Async
	public void deleteGroup(String _id) {

        // remove every user (from this group) before deleting
        for(String username : groupRepository.findOneBy_id(_id).getMembers()){

            User user = userRepository.findOneByUsername(username);
            List<String> groups = user.getGroups();
            groups.remove(_id);
            user.setGroups(groups);
            userRepository.save(user);
        }
        // delete every task associated with the group
        taskService.deleteTasksByGroup(_id);

        groupRepository.deleteOneBy_id(_id);
	}

    //handle null exception in join and leave

    @Async
	public void joinGroup(String groupToken) {

        // add user in groups member list
        Group group = groupRepository.findOneBy_id(jwtUtil.extractClaim(groupToken, Claims::getId));
        User user = userRepository.findOneByEmail(jwtUtil.extractUsername(groupToken));

        System.out.println(group.toString());
        System.out.println(user.toString());

        List <String> members =  group.getMembers();

        members.add(user.getUsername());
        group.setMembers(members);
        groupRepository.save(group);

        // add group in users group list
        List <String> groups  = user.getGroups();

        groups.add(jwtUtil.extractClaim(groupToken, Claims::getId));
        user.setGroups(groups);
        userRepository.save(user);

    }
    

    @Async
	public void leaveGroup(String _id, String token) {

        //remove user from groups member list
        Group group = groupRepository.findOneBy_id(_id);
        List <String> members = group.getMembers();
        members.remove(jwtUtil.extractUsername(token));
        group.setMembers(members);
        groupRepository.save(group);

        //remove group from users group list
        User user = userRepository.findOneByUsername(jwtUtil.extractUsername(token));
        List <String> groups = user.getGroups();
        groups.remove(_id);
        user.setGroups(groups);
        userRepository.save(user);
	}

    public boolean isMember(String groupId, String token){

        Group group = groupRepository.findOneBy_id(groupId);

        return group.getMembers().contains(jwtUtil.extractUsername(token));
    }

    public boolean isAdmin(String groupId, String token){

        Group group = groupRepository.findOneBy_id(groupId);

        return group.getAdmins().contains( jwtUtil.extractUsername( token ) )
           || group.getAdmin().equals( jwtUtil.extractUsername(token) );
    }

	public List<Group> findAll() {
		return groupRepository.findAll();
	}

	public Object findOneBy_id(String _id) {
		return groupRepository.findOneBy_id(_id);
	}

	public String findAdminBy_id(String _id) {
		return groupRepository.findOneBy_id(_id).getAdmin();
	}

    // for group token email is sent in place of username to avoid the json token 
    // being used for authentication
	public boolean validateToken(String token) {
		return userRepository.findOneByEmail(jwtUtil.extractUsername(token)).getEmail().equals(jwtUtil.extractUsername(token))
                && !jwtUtil.isTokenExpired(token);
	}
    
}
