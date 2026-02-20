package it.unipi.bookreel.repository;

import it.unipi.bookreel.DTO.analytic.ListCounterAnalyticDto;
import it.unipi.bookreel.DTO.media.MediaInListsAnalyticDto;
import it.unipi.bookreel.model.FilmsNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;

//for each media list, find the 10 most present films
public interface FilmsNeo4jRepository extends Neo4jRepository<FilmsNeo4j, String> {
    @Query("""
        MATCH (user:User)-[relationship:LIST_ELEMENT]->(f:Films)
        WITH f, relationship,
             CASE
                 WHEN relationship.progress = 1 THEN 'COMPLETED'
                 ELSE 'PLANNED'
             END AS listType
        WITH f, listType, count(DISTINCT relationship) AS listCount
        ORDER BY listType, listCount DESC
        WITH listType, collect({id: f.id, name: f.name, listCount: listCount})[0..10] AS topMedia
        RETURN listType, topMedia
        """)
    List<ListCounterAnalyticDto> findListCounters();

    @Query("""
        MATCH (user:User)-[relationship:LIST_ELEMENT]->(f:Films {id: $filmsId})
        WITH f, relationship,
             CASE
                 WHEN relationship.progress = 1 THEN 'COMPLETED'
                 ELSE 'PLANNED'
             END AS listType
        WITH f, listType, count(DISTINCT relationship) AS listCount
        WITH f, collect({listType: listType, listCount: listCount}) AS appearances
        RETURN f.id AS mediaId, f.name AS mediaName, appearances
        """)
    List<MediaInListsAnalyticDto> findFilmsAppearancesInLists(String filmsId);
}
