package app.services;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import app.models.collections.Group;
import app.models.collections.Task;
import app.models.entity.TaskRequest;
import app.repositories.GroupRepository;
import app.repositories.TaskRepository;
import app.repositories.UserRepository;
import app.util.JwtUtil;

@Service
@EnableAsync
public class TaskService {

    @Autowired
    private MailService mailService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CommentService commentService;

    private static Long DAY = 1000L * 60L * 60L * 24L;


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
                userRepository.findOneByUsername(username).getEmail(),
                username, group.getTitle(), task.getAuthor(),
                task.getDescription().substring(0, Math.min(task.getDescription().length(), 20)),
                task.get_id(), task.getGroupId()
                );
        }
    }

    public boolean isMember(String taskId, String token){

        String groupId = taskRepository.findOneBy_id(taskId).getGroupId();
        Group group = groupRepository.findOneBy_id(groupId);
        return group.getMembers().contains(jwtUtil.extractUsername(token));
    }

    @Async
	public void deleteTask(String id) {

        // delete all comments under a task
        commentService.deleteAllByTask(id);
        
        // delete the task
        taskRepository.deleteBy_id(id);

	}

    @Async
	public void deleteTasksByGroup(String id) {

        List <Task> tasks = taskRepository.findByGroupId(id);
        for(Task task : tasks){
            deleteTask(task.get_id());
        }
	}


    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void checkForDueTasksPerDay() {

        TimerTask repeatedTask = new TimerTask() {
            public void run() {

                System.out.println("Task performed on " + new Date());
                sendDueMail();
            }
        };
        Timer timer = new Timer("Timer");
        
        long delay = 1000L;
        long period = DAY; // 1 Day
        timer.scheduleAtFixedRate(repeatedTask, delay, period);
    }


    @Async
    public void sendDueMail(){

        Date today = new Date();
        List<Task> tasks = taskRepository.findByIsCompletedFalseAndDueTimeBetween(today, new Date(today.getTime() + DAY * 2));

        for(Task task : tasks){
            
            Group group =  groupRepository.findOneBy_id(task.getGroupId());
            List<String> members = group.getMembers();

            // send mail to all members
            String subject = "Task : "+ task.getTitle()  +" Due On " + task.getDueTime();
            String body = "\n Task posted by " + task.getAuthor() + " is due on " + task.getDueTime()
                            + "\n Task : " + task.getShortDescription(20);

            for(String member : members){

                String email = userRepository.findOneByUsername(member).getEmail();

                // send mail
                mailService.sendMail(email, subject, ("Dear " + member + ",\n" + body));
            }
            
            // send mail to admin
            String email = userRepository.findOneByUsername(group.getAdmin()).getEmail();
           
            // send mail
            mailService.sendMail(email, subject, ("Dear " + group.getAdmin() + ",\n" + body));
        }
    
    }

	public String findAuthorBy_id(String id) {
		return taskRepository.findOneBy_id(id).getAuthor();
	}

	public Task updateTask(TaskRequest taskRequest, String taskId) {

      Task task = taskRepository.findOneBy_id(taskId);
      task.setDescription(taskRequest.getDescription());
      task.setCompleted(taskRequest.isCompleted());
      task.setDueTime(taskRequest.getDueTime());
      task.setTitle(taskRequest.getTitle());
      task.setUpdateTime(new Date());

       return taskRepository.save(task);

	}
    
}
