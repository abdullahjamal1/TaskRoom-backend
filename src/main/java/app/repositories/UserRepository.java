package app.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import app.models.entity.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    User findByUsername(String username);

    User findOneByUsernameOrEmail(String username, String username2);

    User findOneByEmail(String email);

    User findOneByToken(String activation);

    User findOneByUsername(String extractUsername);

    List<User> findAll();

	void deleteByUsername(String username);

	String findEmailByUsername(String username);

}