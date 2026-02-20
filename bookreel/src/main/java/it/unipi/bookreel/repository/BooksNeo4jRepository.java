package it.unipi.bookreel.repository;

import it.unipi.bookreel.DTO.analytic.ListCounterAnalyticDto;
import it.unipi.bookreel.DTO.media.MediaInListsAnalyticDto;
import it.unipi.bookreel.model.BooksNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;

//for each media list, find the 10 most present books
public interface BooksNeo4jRepository extends Neo4jRepository<BooksNeo4j, String> {
    @Query("""
        MATCH (user:User)-[relationship:LIST_ELEMENT]->(b:Books)
        WITH b, relationship,
             CASE
                 WHEN relationship.progress = 1 THEN 'COMPLETED'
                 ELSE 'PLANNED'
             END AS listType
        WITH b, listType, count(DISTINCT relationship) AS listCount
        ORDER BY listType, listCount DESC
        WITH listType, collect({id: b.id, name: b.name, listCount: listCount})[0..10] AS topMedia
        RETURN listType, topMedia
        """)
    List<ListCounterAnalyticDto> findListCounters();

    @Query("""
        MATCH (user:User)-[relationship:LIST_ELEMENT]->(b:Books {id: $booksId})
        WITH b, relationship,
             CASE
                 WHEN relationship.progress = 1 THEN 'COMPLETED'
                 ELSE 'PLANNED'
             END AS listType
        WITH b, listType, count(DISTINCT relationship) AS listCount
        WITH b, collect({listType: listType, listCount: listCount}) AS appearances
        RETURN b.id AS mediaId, b.name AS mediaName, appearances
        """)
    List<MediaInListsAnalyticDto> findBooksAppearancesInLists(String booksId);
}
