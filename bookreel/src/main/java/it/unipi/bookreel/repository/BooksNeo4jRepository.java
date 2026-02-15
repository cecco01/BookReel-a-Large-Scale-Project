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
            MATCH (user:User)-[relationship:LIST_ELEMENT]->(Books:Books)
            WITH Books, relationship,
                 CASE
                     WHEN relationship.progress = 1 THEN 'COMPLETED'
                     ELSE 'PLANNED'
                 END AS listType
            WITH  Books, listType, count(DISTINCT relationship) AS listCount
            ORDER BY listType, listCount DESC
            WITH listType, collect({id: Books.id, name: Books.name, count: listCount})[0..10] AS topMedia
            RETURN listType, topMedia
            """)
    List<ListCounterAnalyticDto> findListCounters();

    @Query("""
            MATCH (user:User)-[relationship:LIST_ELEMENT]->(Books:Books {id: $BooksId})
            WITH Books, relationship,
                 CASE
                     WHEN relationship.progress = 1 THEN 'COMPLETED'
                     ELSE 'PLANNED'
                 END AS listType
            WITH  Books, listType, count(DISTINCT relationship) AS listCount
            WITH Books, collect({listType: listType, listCount: listCount}) AS appearances
            RETURN Books.id AS mediaId, Books.name AS mediaName, appearances
            """)
    List<MediaInListsAnalyticDto> findBooksAppearancesInLists(String BooksId);
}
