package app.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import app.models.collections.Comment;

@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {

	void deleteBy_id(String commentId);

	Comment findOneBy_id(String commentId);

	List<Comment> findByTaskIdAndParentId(String taskId, String parentId);

	void deleteByTaskId(String id);
    
}
