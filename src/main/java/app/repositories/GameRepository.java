package app.repositories;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import app.models.entity.Game;
import app.models.entity.User;
import app.models.projections.GameInfo;
import app.models.projections.GameInfoAbstract;

@Repository
public interface GameRepository extends CrudRepository<Game, Long>{
	
	public static String gameInfoQuery = 
			"SELECT g.game_id, g.title, g.last_modified, t.type, if(g.game_id not in (select r.game_id from ratings r) , 0 , rate.rate_table)  AS rating "+
			"FROM game g "+
			"JOIN genre t ON g.genre_id = t.genre_id "+
			",(SELECT (SUM(r.rating)/COUNT(r.rating)) AS rate_table, r.game_id as gameId from ratings r GROUP BY r.game_id) AS rate "+
			"WHERE g.game_id = rate.gameId or g.game_id not in (select r.game_id from ratings r) group by g.game_id ";
	
	public static String gameInfoQueryAdvanced = 
			"SELECT g.*, t.type, if(g.game_id not in (select r.game_id from ratings r) , 0 , rate.rate_table)  AS rating, u.username, rate.ratedBy AS ratedBy "
			+ "FROM game g "
			+ "JOIN genre t ON g.genre_id = t.genre_id "
			+ "JOIN user u ON g.user_id = u.id " + 
			",(SELECT (SUM(r.rating)/COUNT(r.rating)) AS rate_table, COUNT(r.rating) AS ratedBy, r.game_id as gameId from ratings r GROUP BY r.game_id) AS rate "
			+ "WHERE g.game_id = rate.gameId or g.game_id not in (select r.game_id from ratings r) group by g.game_id ";
	
	@Query(value = gameInfoQuery, nativeQuery = true)
	public List<GameInfoAbstract> findAllGame();	
	
	public static String gameInfoQueryByGenre = gameInfoQuery + "HAVING g.genre_id = :genreId;";
	
	@Query(value = gameInfoQueryByGenre, nativeQuery = true)
	public List<GameInfoAbstract> findAllGameByGenre(@Param("genreId") Long genre_id);	
	
	public static String gameInfoQueryById = gameInfoQueryAdvanced + "HAVING g.game_id = :gameId ";
	
	@Query(value = gameInfoQueryById, nativeQuery = true)
	public GameInfo findOneByGameId(@Param("gameId") Long game_id);	
	
	public static String findQueryUserIdByGameId = "SELECT u.id FROM User u, Game g "
			+ "WHERE g.game_id = :gameId AND g.user_id = u.id;";
	
	@Query(value = findQueryUserIdByGameId, nativeQuery = true)
	public Long findGameAuthorUserId(@Param("gameId") Long game_id);

	public static String findGamesWithRatingsAbove = gameInfoQuery + " HAVING rating >= :rating;";
	
	@Query(value = findGamesWithRatingsAbove, nativeQuery = true)	
	public List<GameInfoAbstract> gameListWithRatingAbove(@Param("rating") float rating);

	public static String queryToFilterGameByUpdateDate = gameInfoQuery + " HAVING g.last_modified >= :date;";	
	
	@Query(value = queryToFilterGameByUpdateDate, nativeQuery = true)
	public List<GameInfoAbstract> gameFilterByLastUpdate(@Param("date") Instant date);	
	
}
