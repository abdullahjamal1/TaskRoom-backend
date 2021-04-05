package app.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import app.models.collections.Group;
import app.models.collections.User;
import app.models.entity.GroupRequest;
import app.repositories.GroupRepository;
import app.repositories.UserRepository;
import app.util.JwtUtil;
import io.jsonwebtoken.Claims;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    public Mono<Group> save(GroupRequest groupRequest, String token) {

        Mono<Group> group = groupRepository.save(new Group(groupRequest, jwtUtil.extractUsername(token)))
                .doOnSuccess(g -> {

                    sendInviteToGroup(g, groupRequest);

                });

        // make admin join his group
        Mono<User> user = userRepository.findOneByUsername(jwtUtil.extractUsername(token));

        user = user.map(u -> {
            List<String> g = u.getGroups();
            g.add(group.map(grp -> grp.get_id()).toString());
            u.setGroups(g);
            return u;
        });

        userRepository.save(user);

        return group;
    }

    public void sendInviteToGroup(Group group, GroupRequest groupRequest) {

        for (String username : groupRequest.getMembers()) {

            if (username != group.getAdmin())
                sendInviteToOne(group, username);
        }
    }

    // blocking call in getGroupToken
    public void sendInviteToOne(Group group, String username) {
        
        Mono<User> user = userRepository.findOneByUsername(username);
        
        String inviteToken = getGroupToken(group.get_id(), user.map(u -> u.getEmail() ) );
        
        mailService.sendInvite(user.map(u -> u.getEmail()), username, group.getTitle(), group.getAdmin(), inviteToken);
        
    }
    public String getGroupToken(String groupId, Mono<String> email) {

        // creating a jwt token for invite
        // jti stands for json token id
        Map<String, Object> claims = new HashMap<>();
        claims.put("jti", groupId);
        return jwtUtil.createToken(claims, email.block());
    }

    public Mono<Group> updateGroup(GroupRequest groupRequest, String _id) {

        // extract these to group class
        Mono<Group> group = groupRepository.findOneBy_id(_id);

        group = group.map(g -> {
            g.setDescription(groupRequest.getDescription());
            g.setTitle(groupRequest.getTitle());
            g.setAdmins(groupRequest.getAdmins());
            g.setTheme(groupRequest.getTheme());
            return g;
        });

        // if users are not there in old group then send invite
        for (String username : groupRequest.getMembers()) {

            group.map(g -> {
                if (!g.getMembers().contains(username)) {
                    sendInviteToOne(g, username);
                }
                return g;
            });
        }

        // if users were not there in new group but were there in old group
        // remove them

        group.map(g -> {
            g.getMembers().forEach(username -> {
                if (!groupRequest.getMembers().contains(username)) {

                    Mono<User> user = userRepository.findOneByUsername(username).map(u -> {
                        List<String> groups = u.getGroups();
                        groups.remove(_id);
                        u.setGroups(groups);
                        return u;
                    });

                    userRepository.save(user);
                }
            });
            return g;
        });

        // group.setMembers( groupRequest.getMembers());

        return groupRepository.save(group);
    }

    public void deleteGroup(String _id) {

        // remove every user (from this group) before deleting

        groupRepository.findOneBy_id(_id).map(g -> {
            g.getMembers().forEach(username -> {

                Mono<User> user = userRepository.findOneByUsername(username).map(u -> {
                    List<String> groups = u.getGroups();
                    groups.remove(_id);
                    u.setGroups(groups);
                    return u;
                });
                userRepository.save(user);
            });
            return g;
        });

        // delete every task associated with the group
        taskService.deleteTasksByGroup(_id);

        groupRepository.deleteOneBy_id(_id);
    }

    // handle null exception in join and leave
    public void joinGroup(String groupToken) {

        // add user in groups member list
        Mono<Group> group = groupRepository.findOneBy_id(jwtUtil.extractClaim(groupToken, Claims::getId));
        final Mono<User> user = userRepository.findOneByEmail(jwtUtil.extractUsername(groupToken));

        group.doOnSuccess(g -> {
            List<String> members = g.getMembers();
            members.add(user.map(u -> u.getUsername()).toString());
            g.setMembers(members);
            groupRepository.save(g);
        });

        // add group in users group list
        Mono<User> newUser = user.map(u -> {
            List<String> newGroups = u.getGroups();
            newGroups.add(jwtUtil.extractClaim(groupToken, Claims::getId));
            u.setGroups(newGroups);
            return u;
        });

        userRepository.save(newUser);

    }

    public void leaveGroup(String _id, String token) {

        // remove user from groups member list
        Mono<Group> group = groupRepository.findOneBy_id(_id).map(g -> {
            List<String> members = g.getMembers();
            members.remove(jwtUtil.extractUsername(token));
            g.setMembers(members);
            return g;
        });
        groupRepository.save(group);

        // remove group from users group list
        Mono<User> user = userRepository.findOneByUsername(jwtUtil.extractUsername(token)).map(u -> {
            List<String> groups = u.getGroups();
            groups.remove(_id);
            u.setGroups(groups);
            return u;
        });
        userRepository.save(user);
    }

    public Mono<Boolean> isMember(String groupId, String token) {

        Mono<Group> group = groupRepository.findOneBy_id(groupId);

        return group.map(g -> g.getMembers().contains(jwtUtil.extractUsername(token)) );
    }

    public Mono<Boolean> isAdmin(String groupId, String token) {

        Mono<Group> group = groupRepository.findOneBy_id(groupId);

        return group.map(g -> g.getAdmins().contains(jwtUtil.extractUsername(token))
                         || g.getAdmin().equals(jwtUtil.extractUsername(token)) );
    }

    public Flux<Group> findAll() {
        return groupRepository.findAll();
    }

    public Mono<Group> findOneBy_id(String _id) {
        return groupRepository.findOneBy_id(_id);
    }

    public Mono<String> findAdminBy_id(String _id) {
        return groupRepository.findOneBy_id(_id).map(group -> group.getAdmin());
    }

    // for group token email is sent in place of username to avoid the json token
    // being used for authentication
    public boolean validateToken(String token) {
        return userRepository.findOneByEmail(jwtUtil.extractUsername(token)).map(user -> user.getEmail())
                .equals(jwtUtil.extractUsername(token)) && !jwtUtil.isTokenExpired(token);
    }

}
