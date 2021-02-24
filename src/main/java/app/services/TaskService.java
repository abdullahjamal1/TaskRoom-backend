package app.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import app.models.entity.Task;
import app.models.entity.Group;
import app.repositories.GroupRepository;
import app.repositories.TaskRepository;
import app.repositories.UserRepository;

@Service
@EnableAsync
public class TaskService {

    @Autowired
    private MailService mailService;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private GroupService groupService;

	public Task save(Task task) {
		
        task = taskRepository.save(task);
        sendTaskNotification(task);
        return task;
    }
    
    @Async
    public void sendTaskNotification(Task task){
        
        Group group =  groupRepository.findOneBy_id(task.getGroupId());
        List <String> members = group.getMembers();
    
        for(String username : members){
    
            // sends first 20 letters of task description alongwith notification to each of the members
            mailService.sendTaskNotification(
                userRepository.findEmailByUsername(username),
                username, group.getName(), task.getAuthor(),
                task.getDescription().substring(0, Math.min(task.getDescription().length(), 20)),
                task.get_id(), task.getGroupId()
                );
        }
    }

    public boolean isMember(String taskId, String token){
        return groupService.isMember(taskRepository.findGroupIdBy_id(taskId), token);
    }
    
}
