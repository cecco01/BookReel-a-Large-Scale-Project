package it.unipi.bookreel.repository;

import it.unipi.bookreel.DTO.analytic.ControversialMediaDto;
import it.unipi.bookreel.DTO.analytic.TrendingMediaDto;
import it.unipi.bookreel.DTO.media.MediaAverageDto;
import it.unipi.bookreel.model.FilmsMongo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FilmsMongoRepository extends MongoRepository<FilmsMongo, String> {

    // Restituisce una pagina di film il cui nome contiene il testo fornito, con punteggio medio calcolato
    @Aggregation(pipeline = {
            "{ '$match': { 'name': { $regex: ?0, $options: 'i' } } }",
            "{ '$addFields': { 'averageScore': { $cond: { if: { $eq: ['$numScores', 0] }, then: 0, else: { $divide: ['$sumScores', '$numScores'] } } } } }",
            "{ '$project': { 'id': '$_id', 'name': 1, 'averageScore': 1 } }"
    })
    Slice<MediaAverageDto> findByNameContaining(String name, Pageable pageable);

    
    @Query("{ 'reviews.username': ?0 }")
    @Update("{ '$set': { 'reviews.$.username': ?1 } }")
    void updateReviewsByUsername(String oldUsername, String newUsername);


    // Elimina tutte le recensioni di uno specifico username
    @Query("{ 'reviews.username': ?0 }")
    @Update("{ '$pull': { 'reviews': { 'username': ?0 } } }")
    void deleteReviewsByUsername(String username);


    // Restituisce per ogni genere il film più controverso considerando i film con almeno 5 recensioni
    @Aggregation(pipeline = {
            "{ '$match': { '$expr': { '$gte': ['$numScores', 5] } } }",
            "{ '$addFields': { 'scoreVariance': { '$pow': [{ '$stdDevPop': '$reviews.score' }, 2] } } }",
            "{ '$unwind': '$genres' }",
            "{ '$sort': { 'genres': 1, 'scoreVariance': -1 } }",
            "{ '$group': { " +
                    "   '_id': '$genres', " +
                    "   'Films': { '$first': { 'id': '$_id', 'name': '$name', 'scoreVariance': '$scoreVariance' } } " +
                    "} }",
            "{ '$project': { '_id': 0, 'genre': '$_id', 'id': '$Films.id', 'name': '$Films.name' } }"
    })
    List<ControversialMediaDto> mostControversialFilms();


    // Restituisce i 10 film in maggior declino calcolando la differenza tra punteggio medio totale e medio delle ultime 5 recensioni
    @Aggregation(pipeline = {
            "{ '$addFields': { " +
                    "   'totalAvgScore': { '$cond': { " +
                    "       if: { '$gt': ['$numScores', 0] }, " +
                    "       then: { '$divide': ['$sumScores', '$numScores'] }, " +
                    "       else: 0 " +
                    "   } }, " +
                    "   'reviews': { '$slice': [ { '$sortArray': { 'input': '$reviews', 'sortBy': { 'timestamp': -1 } } }, 5 ] } " +
                    "} }",
            "{ '$addFields': { 'recentAvgScore': { '$avg': '$reviews.score' } } }",
            "{ '$match': { '$expr': { '$lt': ['$recentAvgScore', '$totalAvgScore'] } } }",
            "{ '$addFields': { 'declineScore': { '$subtract': ['$totalAvgScore', '$recentAvgScore'] } } }",
            "{ '$sort': { 'declineScore': -1 } }",
            "{ '$limit': 10 }",
            "{ '$project': { '_id': 0, 'id': '$_id', 'name': '$name', 'declineScore': 1 } }"
    })
    List<TrendingMediaDto> topDecliningFilms();


    // Restituisce i 10 film in maggior miglioramento considerando la differenza tra punteggio medio totale e punteggio medio delle ultime 5 recensioni
    @Aggregation(pipeline = {
            "{ '$addFields': { " +
                    "   'totalAvgScore': { '$cond': { " +
                    "       if: { '$gt': ['$numScores', 0] }, " +
                    "       then: { '$divide': ['$sumScores', '$numScores'] }, " +
                    "       else: 0 " +
                    "   } }, " +
                    "   'reviews': { '$slice': [ { '$sortArray': { 'input': '$reviews', 'sortBy': { 'timestamp': -1 } } }, 5 ] } " +
                    "} }",
            "{ '$addFields': { 'recentAvgScore': { '$avg': '$reviews.score' } } }",
            "{ '$match': { '$expr': { '$gt': ['$recentAvgScore', '$totalAvgScore'] } } }",
            "{ '$addFields': { 'improvementScore': { '$subtract': ['$recentAvgScore', '$totalAvgScore'] } } }",
            "{ '$sort': { 'improvementScore': -1 } }",
            "{ '$limit': 10 }",
            "{ '$project': { '_id': 0, 'id': '$_id', 'name': '$name', 'improvementScore': 1 } }"
    })
    List<TrendingMediaDto> topImprovingFilms();

    // Restituisce i 3 film con punteggio medio più alto, filtrando opzionalmente per genere
    @Aggregation(pipeline = {
            "{ '$addFields': { " +
                    "   'calculatedAvg': { '$cond': { " +
                    "       if: { '$gt': ['$numScores', 0] }, " +
                    "       then: { '$divide': ['$sumScores', '$numScores'] }, " +
                    "       else: 0 " +
                    "   } } " +
                    "} }",
            // Filtra per genere solo se fornito
            "{ '$match': { '$expr': { '$or': [ { '$eq': [?0, null] }, { '$in': [?0, '$genres'] } ] } } }",
            "{ '$sort': { 'calculatedAvg': -1 } }",
            "{ '$limit': 3 }",
            "{ '$project': { 'id': 1, 'name': 1, 'calculatedAvg': 1 } }"
    })
    List<MediaAverageDto> top3FilmsByAverage(String genre);
}