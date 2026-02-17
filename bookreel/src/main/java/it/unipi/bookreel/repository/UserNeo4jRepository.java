package it.unipi.bookreel.repository;

import it.unipi.bookreel.DTO.analytic.SCCAnalyticDto;
import it.unipi.bookreel.DTO.analytic.InfluencersDto;
import it.unipi.bookreel.DTO.media.LikeElementDto;
import it.unipi.bookreel.DTO.media.ListElementDto;
import it.unipi.bookreel.DTO.media.MediaIdNameDto;
import it.unipi.bookreel.DTO.user.UserIdUsernameDto;
import it.unipi.bookreel.model.UserNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.util.List;

@Repository
public interface UserNeo4jRepository extends Neo4jRepository<UserNeo4j, String> {
    @Query("""
             MATCH (u:User)-[l:LIST_ELEMENT]->(f:Films)
             WHERE u.id = $id
               AND (
                 $id = $currentUserId OR
                 u.privacyStatus = 'ALL' OR
                 (u.privacyStatus = 'FOLLOWERS' AND exists {
                     MATCH (follower:User)-[:FOLLOW]->(u)
                     WHERE follower.id = $currentUserId
                 })
               )
             RETURN f.id AS id, f.name AS name, l.progress AS progress
            """)
    List<ListElementDto> findFilmsListsById(String id, String currentUserId);

    @Query("""
            MATCH (u:User)-[l:LIST_ELEMENT]->(b:Books)
            WHERE u.id = $id
              AND (
                $id = $currentUserId OR
                u.privacyStatus = 'ALL' OR
                (u.privacyStatus = 'FOLLOWERS' AND exists {
                    MATCH (follower:User)-[:FOLLOW]->(u)
                    WHERE follower.id = $currentUserId
                })
              )
            RETURN b.id AS id, b.name AS name, l.progress AS progress
            """)
    List<ListElementDto> findBooksListsById(String id, String currentUserId);

    @Query("""
            MATCH (u:User)-[:LIKES]->(f:Films)
            WHERE u.id = $id
              AND (
                $id = $currentUserId OR
                u.privacyStatus = 'ALL' OR
                (u.privacyStatus = 'FOLLOWERS' AND exists {
                    MATCH (follower:User)-[:FOLLOW]->(u)
                    WHERE follower.id = $currentUserId
                })
              )
            RETURN f.id AS id, f.name AS name
            """)
    List<LikeElementDto> findLikedFilmsById(String id, String currentUserId);

    @Query("""
            MATCH (u:User)-[:LIKES]->(b:Books)
            WHERE u.id = $id
              AND (
                $id = $currentUserId OR
                u.privacyStatus = 'ALL' OR
                (u.privacyStatus = 'FOLLOWERS' AND exists {
                    MATCH (follower:User)-[:FOLLOW]->(u)
                    WHERE follower.id = $currentUserId
                })
              )
            RETURN b.id AS id, b.name AS name
            """)
    List<LikeElementDto> findLikedBooksById(String id, String currentUserId);

    
    @Query("MATCH (u:User {id: $userId})" +
            " MATCH (f:Films {id: $mediaId})" +
            " MERGE (u)-[r:LIST_ELEMENT]->(f)" +
            " ON CREATE SET r.progress = 0"+
            " RETURN count(f) > 0")
    boolean addFilmsToList(String userId, String mediaId);


    @Query("MATCH (u:User {id: $userId})" +
            " MATCH (b:Books {id: $mediaId})" +
            " MERGE (u)-[r:LIST_ELEMENT]->(b)" +
            " ON CREATE SET r.progress = 0"+
            "RETURN count(b) > 0")
    boolean addBooksToList(String userId, String mediaId);
    
    @Query("""
            MATCH (u:User {id: $userId})
            MATCH (f:Films {id: $mediaId})
            MERGE (u)-[r:LIKES]->(f)
            RETURN r IS NOT NULL
            """)
    boolean addFilmLike(String userId, String mediaId);
    
    @Query("""
            MATCH (u:User {id: $userId})
            MATCH (b:Books {id: $mediaId})
            MERGE (u)-[r:LIKE]->(b)
            RETURN r IS NOT NULL
            """)
    boolean addBookLike(String userId, String mediaId);


    @Query("""
            MATCH (u:User {id: $userId})-[rel:LIST_ELEMENT]->(f:Films {id: $FilmsId})
            SET rel.progress = $progress
            RETURN COUNT(f) > 0
            """)
    boolean modifyFilmsInList(String userId, String mediaId, int progress);


    @Query("""
            MATCH (u:User {id: $userId})-[rel:LIST_ELEMENT]->(b:Books {id: $BooksId})
            SET rel.progress = $progress
            RETURN count(b) > 0
            """)
    boolean modifyBooksInList(String userId, String mediaId, int progress);


//rimuove un media "Film" dalla lista dell'utente, non elimina il nodo del media
    @Query("MATCH (u:User {id: $userId})-[r:LIST_ELEMENT]->(f:Films {id: $mediaId})" +
            " DELETE r" +
            " RETURN r IS NOT NULL")
    boolean removeFilmsFromList(String userId, String mediaId);

//rimuove un media "Libro" dalla lista dell'utente, non elimina il nodo del media
    @Query("MATCH (u:User {id: $userId})-[r:LIST_ELEMENT]->(b:Books {id: $mediaId})" +
            " DELETE r" +
            " RETURN r IS NOT NULL")
    boolean removeBooksFromList(String userId, String mediaId);

    
    @Query("""
            MATCH (u:User {id: $userId})-[r:LIKES]->(f:Films {id: $mediaId})
            DELETE r
            RETURN r IS NOT NULL
            """)
    boolean removeFilmLike(String userId, String mediaId);
    
    
    @Query("""
            MATCH (u:User {id: $userId})-[r:LIKES]->(b:Books {id: $mediaId})
            DELETE r
            RETURN r IS NOT NULL
            """)
    boolean removeBookLike(String userId, String mediaId);


//trova tutti gli utenti che seguono l’utente con id in input e che hanno il diritto di visualizzare la lista dei followers in base alla privacy dell’utente stesso
    @Query("""
            MATCH (u:User)<-[:FOLLOW]-(f:User)
            WHERE u.id = $id
              AND (
                $id = $currentUserId OR
                u.privacyStatus = 'ALL' OR
                (u.privacyStatus = 'FOLLOWERS' AND exists {
                    MATCH (follower:User)-[:FOLLOW]->(u)
                    WHERE follower.id = $currentUserId
                })
              )
            RETURN f.id AS id, f.username AS username
            """)
    List<UserIdUsernameDto> findFollowersById(String id, String currentUserId);



//trova tutti gli utenti seguiti dall’utente con id passato. Usa la stessa logica di privacy della query precedente, ma restituisce gli utenti che "u" segue invece dei followers
    @Query("""
            MATCH (u:User)-[:FOLLOW]->(f:User)
            WHERE u.id = $id
              AND (
                $id = $currentUserId OR
                u.privacyStatus = 'ALL' OR
                (u.privacyStatus = 'FOLLOWERS' AND exists {
                    MATCH (follower:User)-[:FOLLOW]->(u)
                    WHERE follower.id = $currentUserId
                })
              )
            RETURN f.id AS id, f.username AS username
            """)
    List<UserIdUsernameDto> findFollowedById(String id, String currentUserId);


//crea (o mantiene) la relazione FOLLOW tra due utenti (followerId -> followedId) usando MERGE. Restituisce true se trova il nodo seguito (count(f) > 0), quindi se l’operazione ha un target valido.
    @Query("MATCH (u:User {id: $followerId}), (f:User {id: $followedId}) MERGE (u)-[:FOLLOW]->(f)" +
            " RETURN r IS NOT NULL")
    boolean followUser(String followerId, String followedId);


//elimina la relazione FOLLOW tra followerId e followedId, se esiste. Restituisce true se esisteva il nodo seguito (count(f) > 0), quindi se l’utente target esiste.
    @Query("MATCH (u:User {id: $followerId})-[r:FOLLOW]->(f:User {id: $followedId}) DELETE r" +
            " RETURN r IS NOT NULL")
    boolean unfollowUser(String followerId, String followedId);


// Trova i 10 utenti con gusti più simili basandosi sui media a cui hanno messo LIKES
    @Query("""
        CALL gds.graph.project(
          'similarityGraph',
          ['User', 'Book', 'Film'],
          {
            LIKES: {
              type: 'LIKES',
              orientation: 'UNDIRECTED'
            }
          }
        )
        YIELD graphName
        
        CALL gds.nodeSimilarity.stream('similarityGraph')
        YIELD node1, node2, similarity
        WITH gds.util.asNode(node1) AS user1, gds.util.asNode(node2) AS user2, similarity
        WHERE user1.id = $userId AND user1.id <> user2.id
        RETURN user2.id AS id, user2.username AS username, similarity
        ORDER BY similarity DESC
        LIMIT 10
        """)
    List<UserIdUsernameDto> findUsersWithSimilarTastes(String userId);
/*//costruisce un grafo GDS con nodi User e relazioni LIST_ELEMENT, poi calcola la nodeSimilarity per l’utente userId. Restituisce i 10 utenti piu simili (id, username, similarity), ordinati per similarita decrescente.
    @Query("""
            MATCH (u:User {id: $userId})-[:LIKES]->(target)<-[:LIKES]-(other:User)
            WITH collect(u) + collect(other) AS sourceNodes, collect(target) AS targetNodes
            CALL gds.graph.project(
              'myGraph',
              {
                User: {
                  label: 'User'
                }
              },
              {
                LIST_ELEMENT: {
                  type: 'LIKES'
                }
              }
            )
            YIELD graphName
            
            CALL gds.nodeSimilarity.stream('myGraph')
            YIELD node1, node2, similarity
            WITH gds.util.asNode(node2) AS user1, similarity
            WHERE gds.util.asNode(node1).id = $userId
            RETURN user1.id AS id, user1.username AS username, similarity
            ORDER BY similarity DESC
            LIMIT 10
            """)
    List<UserIdUsernameDto> findUsersWithSimilarTastes(String userId);
    */

//prende i media presenti nelle liste degli utenti seguiti dall’utente userId, filtra per tipo (Films o Books) e per privacy (esclude NOBODY). Conta quante volte ogni media appare e restituisce i 10 più popolari (restituisce [id], nome e count)
    @Query("""
            MATCH (user:User {id: $userId})-[:FOLLOW]->(f:User)-[:LIST_ELEMENT]->(media)
            WHERE ((media:Films AND $mediaType = 'Films') OR (media:Books AND $mediaType = 'Books'))
                  AND f.privacyStatus <> 'NOBODY'
            RETURN media.id AS id, media.name AS name, count(media.id) AS count
            ORDER BY count DESC
            LIMIT 10
            """)
    List<MediaIdNameDto> findPopularMediaAmongFollows(String mediaType, String userId);


//conta i follower di ogni utente ((u)<-[:FOLLOW]-(f)), ordina per numero di follower e restituisce i top 20 (id, username, followersCount)
    @Query("""
            MATCH (u:User)<-[:FOLLOW]-(f:User)
            WITH u, count(f) AS followersCount
            ORDER BY followersCount DESC
            LIMIT 20
            RETURN u.id AS userId, u.username AS username, followersCount
            """)
    List<InfluencersDto> findMostFollowedUsers();



// Trova le SCC (Strongly Connected Components) del grafo dei follow e ritorna gli utenti di ciascuna componente maggiore di 1
    @Query("""
        CALL gds.graph.project(
          'sccGraph',
          'User',
          {
            FOLLOW: {
              type: 'FOLLOW',
              orientation: 'NATURAL'
            }
          }
        )
        YIELD graphName
        
        CALL gds.scc.stream('sccGraph')
        YIELD componentId, nodeId
        WITH componentId, collect(gds.util.asNode(nodeId)) AS users
        WHERE size(users) > 1
        WITH componentId, users, size(users) AS componentSize
        RETURN componentId,
               componentSize,
               [user IN users | {id: user.id, username: user.username}] AS userDetails
        ORDER BY componentSize DESC
        """)
    List<SCCAnalyticDto> findSCC();
/*//costruisce un grafo GDS con relazioni FOLLOW, calcola le componenti fortemente connesse (SCC) e restituisce quelle con piudi 1 utente. Restituisce per ogni componente: id, dimensione, lista di utenti (id e username) ordinati per dimensione decrescente
    @Query("""
            MATCH (source:User)-[:FOLLOW]->(target:User)
            WITH collect(source) AS sourceNodes, collect(target) AS targetNodes
            CALL gds.graph.project(
              'graph',
              ['User'],
              {
                FOLLOW: {
                  type: 'FOLLOW'
                }
              }
            )
            YIELD graphName
            
            CALL gds.scc.stream('graph', {})
            YIELD componentId, nodeId
            WITH componentId, collect(gds.util.asNode(nodeId)) AS users
            WHERE size(users) > 1
            RETURN componentId,
                   size(users) AS componentSize,
                   [user IN users | {id: user.id, username: user.username}] AS userDetails
            ORDER BY componentSize DESC
            """)
    List<SCCAnalyticDto> findSCC();
*/


// Elimina il grafo GDS se esiste (void)
    @Query("CALL gds.graph.drop($graphName)")
    void dropGraph(String graphName);
/*
//elimina il grafo GDS con nome "graphName", se esiste
    @Query("CALL gds.graph.drop($graphName) YIELD graphName RETURN graphName")
    void dropGraph(String graphName);
*/


}

