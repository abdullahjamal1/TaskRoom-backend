package app.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import app.models.collections.User;
import app.models.entity.UsernamesResponse;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    User findByUsername(String username);

    User findOneByUsernameOrEmail(String username, String username2);

    User findOneByEmail(String email);

    User findOneByToken(String activation);

    User findOneByUsername(String extractUsername);

	void deleteByUsername(String username);

	List<User> findByToken(String string);

	User findByEmail(String extractUsername);

}