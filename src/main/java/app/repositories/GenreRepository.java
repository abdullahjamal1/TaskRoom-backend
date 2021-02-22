package app.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import app.models.entity.Genre;

@Repository
public interface GenreRepository extends CrudRepository<Genre, Long>{

	// @Query("select g.genre_id from Genre g where g.genre_id = :genreId")	
	// Genre findOneGenreById(@Param("genreId") Long id);

    // // @Modifying
    // // @Transactional	
	// // @Query("update Genre g set g.type = :type where g.genre_id = :genreId")
	// // Genre updateGenre(@Param("genreId") Long id, @Param("type") String type);


}
