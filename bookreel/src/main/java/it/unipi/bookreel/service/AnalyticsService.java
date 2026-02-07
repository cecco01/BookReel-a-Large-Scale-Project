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
    private final MoviesMongoRepository MoviesMongoRepository;
    private final BooksNeo4jRepository BooksNeo4jRepository;
    private final MoviesNeo4jRepository MoviesNeo4jRepository;

    private final MongoTemplate mongoTemplate;

    @Autowired
    public AnalyticsService(UserMongoRepository userMongoRepository, UserNeo4jRepository userNeo4jRepository,
                            BooksMongoRepository BooksMongoRepository, MoviesMongoRepository MoviesMongoRepository,
                            BooksNeo4jRepository BooksNeo4jRepository, MoviesNeo4jRepository MoviesNeo4jRepository,
                            MonthAnalyticRepository monthAnalyticRepository,
                            MongoTemplate mongoTemplate) {
        this.userMongoRepository = userMongoRepository;
        this.userNeo4jRepository = userNeo4jRepository;
        this.monthAnalyticRepository = monthAnalyticRepository;
        this.BooksMongoRepository = BooksMongoRepository;
        this.MoviesMongoRepository = MoviesMongoRepository;
        this.BooksNeo4jRepository = BooksNeo4jRepository;
        this.MoviesNeo4jRepository = MoviesNeo4jRepository;
        this.mongoTemplate = mongoTemplate;
    }

//c'è ancora da scrivere le analytics

}