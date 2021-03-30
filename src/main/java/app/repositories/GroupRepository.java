package app.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import app.models.collections.Group;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Repository
public interface GroupRepository extends ReactiveMongoRepository<Group, String> {

	Mono<Group> findOneBy_id(String _id);

	void deleteBy_id(String _id);

	Flux<Group> findBy_id(String groupId);

	void deleteOneBy_id(String _id);

	Mono<Group> save(Mono<Group> group);

	Mono<Group> save(Flux<Group> group);

	Mono<Group> findOneBy_id(Mono<String> groupId);

    
}
