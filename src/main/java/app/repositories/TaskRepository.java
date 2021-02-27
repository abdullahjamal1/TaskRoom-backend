package app.repositories;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import app.models.collections.Task;


@Repository
public interface TaskRepository extends MongoRepository<Task, String> {

	List<Task> findByGroupId(String groupId);

	Task findOneBy_idAndGroupId(String id, String taskId);

	Task findOneBy_id(String _id);

	void deleteBy_id(String id);

	Task findOneByGroupId(String id);

	List<Task> findByDueTimeBetween(Date today, Date date);

	List<Task> findByIsCompletedFalseAndDueTimeBetween(Date today, Date date);
    
}
