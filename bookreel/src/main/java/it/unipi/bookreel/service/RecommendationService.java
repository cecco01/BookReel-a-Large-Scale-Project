package it.unipi.bookreel.service;

import it.unipi.bookreel.DTO.media.MediaAverageDto;
import it.unipi.bookreel.DTO.media.MediaIdNameDto;
import it.unipi.bookreel.DTO.user.UserIdUsernameDto;
import it.unipi.bookreel.enumerator.MediaType;
import it.unipi.bookreel.repository.MoviesMongoRepository;
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
    private final MoviesMongoRepository MoviesMongoRepository;

    @Autowired
    public RecommendationService(UserNeo4jRepository userNeo4jRepository, BooksMongoRepository BooksMongoRepository, MoviesMongoRepository MoviesMongoRepository) {
        this.userNeo4jRepository = userNeo4jRepository;
        this.BooksMongoRepository = BooksMongoRepository;
        this.MoviesMongoRepository = MoviesMongoRepository;
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

    public List<MediaAverageDto> getTop10Media(MediaType mediaType, String genre) {
        if (mediaType == MediaType.Movies) {
            return MoviesMongoRepository.findTop10Movies(genre);
        } else {
            return BooksMongoRepository.findTop10Books(genre);
        }
    }
}