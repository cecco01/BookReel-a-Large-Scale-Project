package it.unipi.bookreel.repository;

import it.unipi.bookreel.DTO.analytic.SCCAnalyticDto;
import it.unipi.bookreel.DTO.analytic.InfluencersDto;
import it.unipi.bookreel.DTO.media.ListElementDto;
import it.unipi.bookreel.DTO.media.MediaIdNameDto;
import it.unipi.bookreel.DTO.user.UserIdUsernameDto;
import it.unipi.bookreel.model.UserNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserNeo4jRepository extends Neo4jRepository<UserNeo4j, String> {
    @Query("""
             MATCH (u:User)->(f:Movies)
             WHERE u.id = $id
               AND (
                 $id = $currentUserId OR
                 u.privacyStatus = 'ALL' OR
                 (u.privacyStatus = 'FOLLOWERS' AND exists {
                     MATCH (follower:User)-[:FOLLOW]->(u)
                     WHERE follower.id = $currentUserId
                 })
               )
             RETURN f.id AS id, f.name AS name, f.status AS status, f.duration as duration
            """) //per il momento sui film ho provato a fare questa poi vediamo se abbiamo altri attributi da proiettare
    List<ListElementDto> findMoviesListsById(String id, String currentUserId);

    @Query("""
            MATCH (u:User)->(l:Libri)
            WHERE u.id = $id
              AND (
                $id = $currentUserId OR
                u.privacyStatus = 'ALL' OR
                (u.privacyStatus = 'FOLLOWERS' AND exists {
                    MATCH (follower:User)-[:FOLLOW]->(u)
                    WHERE follower.id = $currentUserId
                })
              )
            RETURN l.id AS id, l.name AS name, l.chapters AS total
            """)//idem per i libri
    List<ListElementDto> findBooksListsById(String id, String currentUserId);


}
