package app.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import app.models.entity.Ratings;
import app.models.entity.RatingsId;

@Repository
public interface RateRepository extends CrudRepository<Ratings, RatingsId> {


}
