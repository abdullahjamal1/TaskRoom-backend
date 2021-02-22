package app.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import app.models.entity.User;
import app.models.projections.UserListResponse;
import app.models.projections.UserResponse;

/**
 * 
 * @author abdullah jamal
 * 
 * crudRepository< TABLE_NAME_IN_DB, PRIMARY_KEY_DATA_TYPE >
 *
 */

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    User findOneByUsername(String name);

    User findOneByEmail(String email);

    User findOneByUsernameOrEmail(String username, String email);

    User findOneByToken(String token);
    
    @Query(value = "select u.id, u.creation_time, u.username, u.first_name, u.last_name, u.role from user u where u.id = :id ;", nativeQuery = true)
    UserResponse findOneById(@Param("id") Long id);

    public static String findAllQuery = "select u.id, u.creation_time, u.username, u.first_name, u.last_name, statistic.uploads" +
    " FROM user u, " +
    " (select count(*) as uploads, g.user_id as id from game g group by g.user_id) as statistic" +
    " where statistic.id = u.id ;";


    @Query(value = findAllQuery, nativeQuery = true)
	Iterable<UserListResponse> findAllUsers();

    /*
     * Queries that require a `@Modifying` annotation include INSERT, UPDATE, DELETE, and DDLstatements.
     *  @Transactional Describes a transaction attribute on an individual method or on a class
     */

    @Modifying
    @Transactional
    @Query("update User u set u.lastLogin = CURRENT_TIMESTAMP where u.username = ?1")
    int updateLastLogin(String username);

}