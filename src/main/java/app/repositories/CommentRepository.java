package app.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import app.models.entity.Comment;

@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {

	void deleteBy_id(String commentId);

	Object findAuthorBy_id(String commentId);

	Object findByTaskIdAndParentId(String taskId, String parentId);
    
}
