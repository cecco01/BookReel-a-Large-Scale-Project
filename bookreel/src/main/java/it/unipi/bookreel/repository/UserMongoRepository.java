package it.unipi.bookreel.repository;

import it.unipi.bookreel.DTO.user.UserIdUsernameDto;
import it.unipi.bookreel.model.MonthAnalytic;
import it.unipi.bookreel.model.UserMongo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface UserMongoRepository extends MongoRepository<UserMongo, String> {

    // Ricerca utenti per username
    @Query("{ 'username': { $regex: ?0, $options: 'i' } }")
    Slice<UserIdUsernameDto> findByUsernameContaining(String username, Pageable pageable);


    // Trova utente data l'email
    UserMongo findByEmail(String email);


    // Verifica se esiste un utente con l'email fornita
    boolean existsByEmail(String email);


    // Verifica se esiste un utente con l'username fornito
    boolean existsByUsername(String username);


    // Aggiunge un follower a un utente specifico
    @Update("{ $addToSet: { followers: ?1 } }")
    void findAndPushFollowerById(String id, String followerId);


    // Rimuove un follower da un utente specifico
    @Update("{ $pull: { followers: ?1 } }")
    void findAndPullFollowerById(String id, String followerId);


    // Rimuove un utente da tutti i followers di altri utenti
    @Query("{ 'followers': ?0 }")
    @Update("{ $pull: { followers: ?0 } }")
    void deleteUserFromFollowers(String id);


    // Restituisce per ogni anno il mese con il maggior numero di utenti creati a partire da una certa data
    @Aggregation(pipeline = {
            "{ '$match': { 'createdAt': { '$gte': ?0 } } }",
            "{ '$group': { '_id': { 'anno': { '$year': '$createdAt' }, 'mese': { '$month': '$createdAt' } }, 'totaleUtenti': { '$sum': 1 } } }",
            "{ '$sort': { '_id.anno': 1, 'totaleUtenti': -1 } }",
            "{ '$group': { '_id': '$_id.anno', 'meseTop': { '$first': { 'mese': '$_id.mese', 'conteggio': '$totaleUtenti' } } } }",
            "{ '$project': { '_id': 0, 'anno': '$_id', 'mese': '$meseTop.mese', 'conteggio': '$meseTop.conteggio' } }"
    })
    List<MonthAnalytic> topMonthsByYearSince(Date fromDate);
}