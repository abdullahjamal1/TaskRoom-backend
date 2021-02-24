package app.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import app.models.entity.Group;
import app.models.entity.GroupRequest;
import app.models.entity.User;
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

    // throw error in save and delete if an un-existing user is added

    // switch username in JWT groupToken to email/userID

	public Group save(GroupRequest groupRequest, String token) {

        Group group = new Group(groupRequest);
        group.setAdmin(jwtUtil.extractUsername(token));
        // set creation time
        group = groupRepository.save(group);

        sendInviteToGroup(group, groupRequest);
        
        return group;
	}
    
    @Async
    public void sendInviteToGroup(Group group, GroupRequest groupRequest){
        
        for(String username : groupRequest.getMembers()){
            
            sendInviteToOne(group, username);
        }      
    }
    
    @Async
    public void sendInviteToOne(Group group, String username){
        // creating a jwt token for invite
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", group.get_id());
        String inviteToken = jwtUtil.createToken(claims, username);
        mailService.sendInvite(userRepository.findEmailByUsername(username), username, group.getName(), group.getAdmin(), inviteToken);

    }

	public Group updateGroup(GroupRequest groupRequest, String _id) {
		
        Group group = groupRepository.findOneBy_id(_id);
        group.setDescription(groupRequest.getDescription());
        group.setName(groupRequest.getName());
        group.setAdmins(groupRequest.getAdmins());
        
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

        groupRepository.deleteBy_id(_id);
	}

    @Async
	public void joinGroup(String groupToken) {

        // add user in groups member list
        Group group = groupRepository.findOneBy_id(jwtUtil.extractClaim(groupToken, Claims::getId));
        List <String> members = group.getMembers();
        members.add(jwtUtil.extractUsername(groupToken));
        group.setMembers(members);
        groupRepository.save(group);

        // add group in users group list
        User user = userRepository.findOneByUsername(jwtUtil.extractUsername(groupToken));
        List <String> groups = user.getGroups();
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

        return groupRepository.findMembersBy_id(groupId).contains(jwtUtil.extractUsername(token)) ||
                groupRepository.findAdminBy_id(groupId).equals(jwtUtil.extractUsername(token));
    }

    public boolean isAdmin(String groupId, String token){
        return groupRepository.findAdminsBy_id( groupId ).contains( jwtUtil.extractUsername( token ) )
           || groupRepository.findAdminBy_id( groupId ).equals( jwtUtil.extractUsername(token) );
    }
    
}
