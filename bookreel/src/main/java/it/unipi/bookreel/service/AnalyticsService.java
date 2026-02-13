package it.unipi.bookreel.service;

import it.unipi.bookreel.DTO.analytic.*;
import it.unipi.bookreel.DTO.media.MediaInListsAnalyticDto;
import it.unipi.bookreel.enumerator.MediaType;
import it.unipi.bookreel.model.MonthAnalytic;
import it.unipi.bookreel.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
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

    @Autowired
    public AnalyticsService(UserMongoRepository userMongoRepository, UserNeo4jRepository userNeo4jRepository,
                            BooksMongoRepository BooksMongoRepository, FilmsMongoRepository FilmsMongoRepository,
                            BooksNeo4jRepository BooksNeo4jRepository, FilmsNeo4jRepository FilmsNeo4jRepository,
                            MonthAnalyticRepository monthAnalyticRepository,
                            MongoTemplate mongoTemplate) {
        this.userMongoRepository = userMongoRepository;
        this.userNeo4jRepository = userNeo4jRepository;
        this.monthAnalyticRepository = monthAnalyticRepository;
        this.BooksMongoRepository = BooksMongoRepository;
        this.FilmsMongoRepository = FilmsMongoRepository;
        this.BooksNeo4jRepository = BooksNeo4jRepository;
        this.FilmsNeo4jRepository = FilmsNeo4jRepository;
        this.mongoTemplate = mongoTemplate;
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
        if (mediaType == MediaType.Books) {
            return BooksMongoRepository.mostControversialBooks();
        } else {
            return FilmsMongoRepository.mostControversialFilms();
        }
    }

// per ogni media, quali stanno peggiorando in temrini di punteggio
    public List<TrendingMediaDto> getDecliningMedia(MediaType mediaType) {
        if (mediaType == MediaType.Books) {
            return BooksMongoRepository.topDecliningBooks();
        } else {
            return FilmsMongoRepository.topDecliningFilms();
        }
    }

// per ogni media, quali stanno migliorando in temrini di punteggio
    public List<TrendingMediaDto> getImprovingMedia(MediaType mediaType) {
        if (mediaType == MediaType.Books) {
            return BooksMongoRepository.topImprovingBooks();
        } else {
            return FilmsMongoRepository.topImprovingFilms();
        }
    }

// quali sono le componenti fortemente connesse (SCC) all'interno del grafo degli utenti e delle loro liste, e chi sono gli influencer più importanti (utenti con più follower)
    public List<SCCAnalyticDto> getSCC() {
        List<SCCAnalyticDto> scc = userNeo4jRepository.findSCC();
        userNeo4jRepository.dropGraph("graph");
        return scc;
    }

// quali sono gli utenti più seguiti
    public List<InfluencersDto> getInfluencers() {
        return userNeo4jRepository.findMostFollowedUsers();
    }

// per ogni media, quante liste degli utenti lo contengono
    public List<ListCounterAnalyticDto> getListCounter(MediaType mediaType) {
        if (mediaType == MediaType.Books) {
            return BooksNeo4jRepository.findListCounters();
        } else {
            return FilmsNeo4jRepository.findListCounters();
        }
    }

// per un dato media, in quante e quali liste degli utenti è presente
    public List<MediaInListsAnalyticDto> getMediaInLists(MediaType mediaType, String mediaId) {
        if (mediaType == MediaType.Books) {
            return BooksNeo4jRepository.findBooksAppearancesInLists(mediaId);
        } else {
            return FilmsNeo4jRepository.findFilmsAppearancesInLists(mediaId);
        }
    }
}