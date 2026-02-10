package it.unipi.bookreel.repository;

import it.unipi.bookreel.DTO.analytic.ListCounterAnalyticDto;
import it.unipi.bookreel.DTO.media.MediaInListsAnalyticDto;
import it.unipi.bookreel.model.FilmsNeo4j;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;

//da modificare visto che non abbiamo progress!!
/*
public interface FilmsNeo4jRepository extends Neo4jRepository<FilmsNeo4j, String> {
    @Query("""
            MATCH (user:User)-[relationship:LIST_ELEMENT]->(Films:Films)
            WITH Films, relationship,
                 CASE
                     WHEN relationship.progress = 0 THEN 'PLANNED'
                     WHEN relationship.progress = Films.episodes AND Films.status = 'COMPLETE' THEN 'COMPLETED'
                     ELSE 'IN_PROGRESS'
                 END AS listType
            WITH  Films, listType, count(DISTINCT relationship) AS listCount
            ORDER BY listType, listCount DESC
            WITH listType, collect({id: Films.id, name: Films.name, count: listCount})[0..10] AS topMedia
            RETURN listType, topMedia
            """)
    List<ListCounterAnalyticDto> findListCounters();

    @Query("""
            MATCH (user:User)-[relationship:LIST_ELEMENT]->(Films:Films {id: $FilmsId})
            WITH Films, relationship,
                 CASE
                     WHEN relationship.progress = 0 THEN 'PLANNED'
                     WHEN relationship.progress = Films.episodes AND Films.status = 'COMPLETE' THEN 'COMPLETED'
                     ELSE 'IN_PROGRESS'
                 END AS listType
            WITH  Films, listType, count(DISTINCT relationship) AS listCount
            WITH Films, collect({listType: listType, listCount: listCount}) AS appearances
            RETURN Films.id AS mediaId, Films.name AS mediaName, appearances
            """)
    List<MediaInListsAnalyticDto> findFilmsAppearancesInLists(String FilmsId);
}
*/