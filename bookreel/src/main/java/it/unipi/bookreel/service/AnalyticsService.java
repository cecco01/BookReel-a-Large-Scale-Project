package it.unipi.bookreel.service;

import it.unipi.bookreel.DTO.analytic.*;
import it.unipi.bookreel.DTO.media.MediaInListsAnalyticDto;
import it.unipi.bookreel.DTO.user.UserIdUsernameDto;
import it.unipi.bookreel.enumerator.MediaType;
import it.unipi.bookreel.model.MonthAnalytic;
import it.unipi.bookreel.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class AnalyticsService {

    private final UserMongoRepository userMongoRepository;
    private final UserNeo4jRepository userNeo4jRepository;
    private final MonthAnalyticRepository monthAnalyticRepository;
    private final BooksMongoRepository BooksMongoRepository;
    private final FilmsMongoRepository FilmsMongoRepository;
    private final BooksNeo4jRepository BooksNeo4jRepository;
    private final FilmsNeo4jRepository FilmsNeo4jRepository;
    private final MongoTemplate mongoTemplate;
    private final Neo4jClient neo4jClient;

    @Autowired
    public AnalyticsService(UserMongoRepository userMongoRepository,
                            UserNeo4jRepository userNeo4jRepository,
                            BooksMongoRepository BooksMongoRepository,
                            FilmsMongoRepository FilmsMongoRepository,
                            BooksNeo4jRepository BooksNeo4jRepository,
                            FilmsNeo4jRepository FilmsNeo4jRepository,
                            MonthAnalyticRepository monthAnalyticRepository,
                            MongoTemplate mongoTemplate,
                            @Qualifier("neo4jClient") Neo4jClient neo4jClient) {
        this.userMongoRepository = userMongoRepository;
        this.userNeo4jRepository = userNeo4jRepository;
        this.monthAnalyticRepository = monthAnalyticRepository;
        this.BooksMongoRepository = BooksMongoRepository;
        this.FilmsMongoRepository = FilmsMongoRepository;
        this.BooksNeo4jRepository = BooksNeo4jRepository;
        this.FilmsNeo4jRepository = FilmsNeo4jRepository;
        this.mongoTemplate = mongoTemplate;
        this.neo4jClient = neo4jClient;
    }

//ANALYTICS METHODS:
// per ogni anno, qual è stato il mese con più registrazioni e quante registrazioni ci sono state in quel mese
    public List<MonthAnalytic> getMonthlyRegistrations() {
        MonthAnalytic maxDocument = monthAnalyticRepository.findTopByOrderByYearDesc();
        int lastYearCalculated = maxDocument != null ? maxDocument.getYear() : 2000;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, lastYearCalculated);
        cal.set(Calendar.DAY_OF_YEAR, 1);
        Date firstDay = cal.getTime();
        List<MonthAnalytic> results = userMongoRepository.topMonthsByYearSince(firstDay);
        monthAnalyticRepository.saveAll(results);
        return monthAnalyticRepository.findAllByOrderByYear();
    }

// per ogni media, qual è la variazione di valutazioni (in termini di punteggio medio) rispetto al mese precedente, e quali sono i media più controversi (con la maggiore varianza nelle valutazioni)
    public List<ControversialMediaDto> getControversialMedia(MediaType mediaType) {
        if (mediaType == MediaType.BOOKS) {
            return BooksMongoRepository.mostControversialBooks();
        } else {
            return FilmsMongoRepository.mostControversialFilms();
        }
    }

// per ogni media, quali stanno peggiorando in termini di punteggio
    public List<TrendingMediaDto> getDecliningMedia(MediaType mediaType) {
        if (mediaType == MediaType.BOOKS) {
            return BooksMongoRepository.topDecliningBooks();
        } else {
            return FilmsMongoRepository.topDecliningFilms();
        }
    }

// per ogni media, quali stanno migliorando in termini di punteggio
    public List<TrendingMediaDto> getImprovingMedia(MediaType mediaType) {
        if (mediaType == MediaType.BOOKS) {
            return BooksMongoRepository.topImprovingBooks();
        } else {
            return FilmsMongoRepository.topImprovingFilms();
        }
    }

// quali sono le componenti fortemente connesse (SCC) all'interno del grafo degli utenti e delle loro liste, e chi sono gli influencer più importanti (utenti con più follower)
    public List<SCCAnalyticDto> getSCC() {
        try {
            userNeo4jRepository.dropGraph("graph");
        } catch (Exception ignored) {}

        neo4jClient.query("""
            CALL gds.graph.project(
              'graph',
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

        List<SCCAnalyticDto> scc = neo4jClient.query("""
            CALL gds.scc.stream('graph')
            YIELD componentId, nodeId
            WITH componentId, collect(gds.util.asNode(nodeId)) AS users
            WHERE size(users) > 1
            WITH componentId, users, size(users) AS componentSize
            RETURN componentId,
                   componentSize,
                   [user IN users | {id: user.id, username: user.username}] AS userDetails
            ORDER BY componentSize DESC
            """)
                .fetchAs(SCCAnalyticDto.class)
                .mappedBy((typeSystem, record) -> {
                    List<UserIdUsernameDto> users = record.get("userDetails")
                            .asList(v -> new UserIdUsernameDto(
                                    v.get("id").asString(),
                                    v.get("username").asString()
                            ));
                    return new SCCAnalyticDto(
                            record.get("componentId").asInt(),
                            record.get("componentSize").asInt(),
                            users
                    );
                })
                .all()
                .stream()
                .toList();

        try {
            userNeo4jRepository.dropGraph("graph");
        } catch (Exception ignored) {}

        return scc;
    }

// quali sono gli utenti più seguiti
    public List<InfluencersDto> getInfluencers() {
        return userNeo4jRepository.findMostFollowedUsers();
    }

// per ogni media, quante liste degli utenti lo contengono
    public List<ListCounterAnalyticDto> getListCounter(MediaType mediaType) {
        if (mediaType == MediaType.BOOKS) {
            return BooksNeo4jRepository.findListCounters();
        } else {
            return FilmsNeo4jRepository.findListCounters();
        }
    }

// per un dato media, in quante e quali liste degli utenti è presente
    public List<MediaInListsAnalyticDto> getMediaInLists(MediaType mediaType, String mediaId) {
        if (mediaType == MediaType.BOOKS) {
            return BooksNeo4jRepository.findBooksAppearancesInLists(mediaId);
        } else {
            return FilmsNeo4jRepository.findFilmsAppearancesInLists(mediaId);
        }
    }
}