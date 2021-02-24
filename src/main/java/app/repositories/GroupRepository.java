package app.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import app.models.entity.Group;
import app.models.entity.Task;

@Repository
public interface GroupRepository extends MongoRepository<Group, String> {

	Group findOneBy_id(String _id);

	String findAdminBy_id(String _id);

	void deleteBy_id(String _id);

	List<String> findMembersBy_id(String groupId);

	String findNameBy_id(String groupId);

	List<Task> findAdminsBy_id(String groupId);
    
}
