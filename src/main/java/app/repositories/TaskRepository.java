package app.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import app.models.entity.Task;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {

	List<Task> findByGroupId(String groupId);

	Task findOneBy_idAndGroupId(String id, String taskId);

	String findAuthorBy_id(String taskId);

	Task findOneBy_id(String _id);

	String findGroupIdBy_id(String taskId);

    
}
