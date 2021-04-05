package app.repositories;

import java.util.Date;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import app.models.collections.Task;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Repository
public interface TaskRepository extends ReactiveMongoRepository<Task, String> {

	Flux<Task> findByGroupId(String groupId);

	Mono<Task> findOneBy_idAndGroupId(String id, String taskId);

	Mono<Task> findOneBy_id(String _id);

	void deleteBy_id(String id);

	Mono<Task> findOneByGroupId(String id);

	Flux<Task> findByDueTimeBetween(Date today, Date date);

	Flux<Task> findByIsCompletedFalseAndDueTimeBetween(Date today, Date date);

	Mono<Task> save(Mono<Task> task_);
    
}
