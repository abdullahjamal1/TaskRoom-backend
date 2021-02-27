package app.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import app.models.collections.Group;


@Repository
public interface GroupRepository extends MongoRepository<Group, String> {

	Group findOneBy_id(String _id);

	void deleteBy_id(String _id);

	List<Group> findBy_id(String groupId);

	void deleteOneBy_id(String _id);
    
}
