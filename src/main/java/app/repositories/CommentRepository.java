package app.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import app.models.collections.Comment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CommentRepository extends ReactiveMongoRepository<Comment, String> {

	void deleteBy_id(String commentId);

	Mono<Comment> findOneBy_id(String commentId);

	Flux<Comment> findByTaskIdAndParentId(String taskId, String parentId);

	void deleteByTaskId(String id);
    
}
