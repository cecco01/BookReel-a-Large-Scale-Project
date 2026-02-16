package it.unipi.bookreel.service;

import it.unipi.bookreel.DTO.media.MediaAverageDto;
import it.unipi.bookreel.DTO.media.MediaIdNameDto;
import it.unipi.bookreel.DTO.user.UserIdUsernameDto;
import it.unipi.bookreel.enumerator.MediaType;
import it.unipi.bookreel.repository.FilmsMongoRepository;
import it.unipi.bookreel.repository.BooksMongoRepository;
import it.unipi.bookreel.repository.UserNeo4jRepository;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class RecommendationService {

    private final UserNeo4jRepository userNeo4jRepository;
    private final BooksMongoRepository BooksMongoRepository;
    private final FilmsMongoRepository FilmsMongoRepository;

    @Autowired
    public RecommendationService(UserNeo4jRepository userNeo4jRepository, BooksMongoRepository BooksMongoRepository, FilmsMongoRepository FilmsMongoRepository) {
        this.userNeo4jRepository = userNeo4jRepository;
        this.BooksMongoRepository = BooksMongoRepository;
        this.FilmsMongoRepository = FilmsMongoRepository;
    }

    public List<UserIdUsernameDto> getUsersWithSimilarTastes(String userId) {
        userNeo4jRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found"));
        List<UserIdUsernameDto> users = userNeo4jRepository.findUsersWithSimilarTastes(userId);
        userNeo4jRepository.dropGraph("myGraph");
        return users;
    }

    public List<MediaIdNameDto> getPopularMediaAmongFollows(MediaType mediaType, String userId) {
        return userNeo4jRepository.findPopularMediaAmongFollows(mediaType.toString(), userId);
    }

    public List<MediaAverageDto> getTop3Media(MediaType mediaType, String genre) {
        if (mediaType == MediaType.FILMS) {
            return FilmsMongoRepository.top3FilmsByAverage(genre);
        } else {
            return BooksMongoRepository.top3BooksByAverage(genre);
        }
    }
}