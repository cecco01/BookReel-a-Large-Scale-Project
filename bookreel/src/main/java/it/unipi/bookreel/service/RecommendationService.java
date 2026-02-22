package it.unipi.bookreel.service;

import it.unipi.bookreel.DTO.media.MediaAverageDto;
import it.unipi.bookreel.DTO.media.MediaIdNameDto;
import it.unipi.bookreel.DTO.user.UserIdUsernameDto;
import org.springframework.data.neo4j.core.Neo4jClient;
import it.unipi.bookreel.DTO.user.UserIdUsernameDtoSimilarity;
import it.unipi.bookreel.enumerator.MediaType;
import it.unipi.bookreel.repository.FilmsMongoRepository;
import it.unipi.bookreel.repository.BooksMongoRepository;
import it.unipi.bookreel.repository.UserNeo4jRepository;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class RecommendationService {

    private final UserNeo4jRepository userNeo4jRepository;
    private final BooksMongoRepository BooksMongoRepository;
    private final FilmsMongoRepository FilmsMongoRepository;
    private final Neo4jClient neo4jClient;

    @Autowired
    public RecommendationService(UserNeo4jRepository userNeo4jRepository,
                                 BooksMongoRepository booksMongoRepository,
                                 FilmsMongoRepository filmsMongoRepository,
                                 Neo4jClient neo4jClient) {
        this.userNeo4jRepository = userNeo4jRepository;
        this.BooksMongoRepository = booksMongoRepository;
        this.FilmsMongoRepository = filmsMongoRepository;
        this.neo4jClient = neo4jClient;
    }

    /*
    public List<UserIdUsernameDtoSimilarity> getUsersWithSimilarTastes(String userId) {
        userNeo4jRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found"));
        try {
            userNeo4jRepository.dropGraph("myGraph");
        } catch (Exception ignored) {
        }

        neo4jClient.query("""
                        CALL gds.graph.project(
                          'myGraph',
                          'User',
                          {
                            FOLLOW: {
                              type: 'FOLLOW',
                              orientation: 'NATURAL'
                            }
                          }
                        )
                        YIELD graphName
                        RETURN graphName
                        """)
                .run();

        List<UserIdUsernameDtoSimilarity> users = neo4jClient.query("""
                            CALL gds.nodeSimilarity.stream('myGraph')
                            YIELD node1, node2, similarity
                            WITH gds.util.asNode(node1) AS user1,
                                 gds.util.asNode(node2) AS user2,
                                 similarity
                            WHERE user1.id = $userId AND user1.id <> user2.id
                            RETURN user2.id AS id,
                                   user2.username AS username,
                                   similarity AS similarity
                            ORDER BY similarity DESC
                            LIMIT 10
                        """)
                .bind(userId).to("userId")
                .fetch()
                .all()
                .stream()
                .map(record -> new UserIdUsernameDtoSimilarity(
                        (String) record.get("id"),
                        (String) record.get("username"),
                        ((Number) record.get("similarity")).doubleValue()
                ))
                .toList();


        userNeo4jRepository.dropGraph("myGraph");

        return users;
    }
    */
    public List<UserIdUsernameDtoSimilarity> getUsersWithSimilarTastes(String userId) {
        userNeo4jRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found"));
        try {
            userNeo4jRepository.dropGraph("myGraph");
        } catch (Exception ignored) {
        }

        neo4jClient.query("""
                    CALL gds.graph.project(
                      'myGraph',
                      ['User', 'Books', 'Films'],
                      {
                        LIKES: {
                          type: 'LIKES',
                          orientation: 'UNDIRECTED'
                        }
                      }
                    )
                    YIELD graphName
                    RETURN graphName
                    """)
                .run();

        List<UserIdUsernameDtoSimilarity> users = neo4jClient.query("""
                        CALL gds.nodeSimilarity.stream('myGraph')
                        YIELD node1, node2, similarity
                        WITH gds.util.asNode(node1) AS user1,
                             gds.util.asNode(node2) AS user2,
                             similarity
                        WHERE user1.id = $userId
                          AND user2.username IS NOT NULL
                          AND NOT EXISTS { MATCH (user1)-[:FOLLOW]->(user2) }
                        RETURN user2.id AS id,
                               user2.username AS username,
                               similarity AS similarity
                        ORDER BY similarity DESC
                        LIMIT 10
                    """)
                .bind(userId).to("userId")
                .fetch()
                .all()
                .stream()
                .map(record -> new UserIdUsernameDtoSimilarity(
                        (String) record.get("id"),
                        (String) record.get("username"),
                        ((Number) record.get("similarity")).doubleValue()
                ))
                .toList();

        userNeo4jRepository.dropGraph("myGraph");

        return users;
    }

    public List<MediaIdNameDto> getPopularMediaAmongFollows(MediaType mediaType, String userId) {
        return userNeo4jRepository.findPopularMediaAmongFollows(mediaType.toString(), userId);
    }

    /*
     public List<MediaAverageDto> getTop3Media(MediaType mediaType, String genre) {
         if (mediaType == MediaType.FILMS) {
             return genre == null
                     ? FilmsMongoRepository.top3FilmsByAverage()
                     : FilmsMongoRepository.top3FilmsByAverageAndGenre(genre);
         } else {
             return genre == null
                     ? BooksMongoRepository.top3BooksByAverage()
                     : BooksMongoRepository.top3BooksByAverageAndGenre(genre);
         }
     }
     */
    public List<MediaAverageDto> getTop3Media(MediaType mediaType, String genre) {
        if (mediaType == MediaType.FILMS) {
            return FilmsMongoRepository.top3FilmsByAverage(genre);
        } else {
            return BooksMongoRepository.top3BooksByAverage(genre);
        }
    }
}