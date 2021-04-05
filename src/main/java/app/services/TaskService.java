package app.services;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import app.models.collections.Task;
import app.models.entity.TaskRequest;
import app.repositories.GroupRepository;
import app.repositories.TaskRepository;
import app.repositories.UserRepository;
import app.util.JwtUtil;
import reactor.core.publisher.Mono;

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


	public Mono<Task> save(Task task) {
		
        Mono<Task> monoTask = taskRepository.save(task);
        sendTaskNotification(task);
        return monoTask;
    }

    public void sendTaskNotification(Task task){
        
        groupRepository.findOneBy_id(task.getGroupId()).doOnSuccess(group ->{
            
            group.getMembers().forEach(username ->{
                // sends first 20 letters of task description alongwith notification to each of the members
                mailService.sendTaskNotification(
                    userRepository.findOneByUsername(username).map(user -> user.getEmail()),
                    username, group.getTitle(),task
                    );
            });
        });
 
    }

    public Mono<Boolean> isMember(String taskId, String token) {

        Mono<String> groupId = taskRepository.findOneBy_id(taskId).map(task -> task.getGroupId() );
       
        return groupRepository.findOneBy_id(groupId).map(
            group -> group.getMembers().contains(jwtUtil.extractUsername(token))
            );
    }

	public void deleteTask(String id) {

        // delete all comments under a task
        commentService.deleteAllByTask(id);
        
        // delete the task
        taskRepository.deleteBy_id(id);

	}

	public void deleteTasksByGroup(String id) {

        // for each task in a group
        taskRepository.findByGroupId(id).map(task ->{
                deleteTask(task.get_id());
                return task;
        });
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
        taskRepository.findByIsCompletedFalseAndDueTimeBetween(today, new Date(today.getTime() + DAY * 2))
        .map(task ->{

            // send mail to all members
            String subject = "Task : "+ task.getTitle()  +" Due On " + task.getDueTime();
            String body = "\n Task posted by " + task.getAuthor() + " is due on " + task.getDueTime()
                            + "\n Task : " + task.getShortDescription(20);
            
            groupRepository.findOneBy_id(task.getGroupId()).doOnSuccess(group ->{
                
                group.getMembers().forEach(member ->{
                    
                    Mono<String> email = userRepository.findOneByUsername(member).map(user -> user.getEmail() );
                                // send mail
                    mailService.sendMail(email.block(), subject, ("Dear " + member + ",\n" + body));

                });
                
                // send mail to admin
                Mono<String> email = userRepository.findOneByUsername(group.getAdmin()).map(user -> user.getEmail());
                // send mail
                mailService.sendMail(email.block(), subject, ("Dear " + group.getAdmin() + ",\n" + body));
            });
            return task;

        });

    
    }

	public Mono<String> findAuthorBy_id(String id) {
		return taskRepository.findOneBy_id(id).map(task -> task.getAuthor());
	}

	public Mono<Task> updateTask(TaskRequest taskRequest, String taskId) {

        Mono<Task> task_ =  taskRepository.findOneBy_id(taskId).map(
            task -> {
                task.setDescription(taskRequest.getDescription());
                task.setCompleted(taskRequest.isCompleted());
                task.setDueTime(taskRequest.getDueTime());
                task.setTitle(taskRequest.getTitle());
                task.setUpdateTime(new Date());
                return task;
            }
    );
        return taskRepository.save(task_);

	}
    
}
