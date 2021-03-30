package app.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import app.models.collections.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveMongoRepository<User, String> {


    Mono<User> findByUsername(String username);

    User findOneByUsernameOrEmail(String username, String email);

    Mono<User> findOneByEmail(String email);

    Mono<User> findOneByToken(String activation);

    Mono<User> findOneByUsername(String extractUsername);

	void deleteByUsername(String username);

	Flux<User> findByToken(String string);

    Mono<User> findByEmail(String extractUsername);

	void save(Mono<User> user);

	Mono<User> findByUsername(Mono<Object> map);




}