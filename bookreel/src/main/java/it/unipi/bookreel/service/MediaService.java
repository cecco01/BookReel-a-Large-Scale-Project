package it.unipi.bookreel.service;

import it.unipi.bookreel.DTO.media.*;
import it.unipi.bookreel.enumerator.MediaType;
import it.unipi.bookreel.model.*;
import it.unipi.bookreel.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class MediaService {
    private final BooksNeo4jRepository BooksNeo4jRepository;
    private final MoviesNeo4jRepository MoviesNeo4jRepository;
    private final BooksMongoRepository BooksMongoRepository;
    private final MoviesMongoRepository MoviesMongoRepository;

    @Autowired
    public MediaService(BooksNeo4jRepository BooksNeo4jRepository, MoviesNeo4jRepository MoviesNeo4jRepository, BooksMongoRepository BooksMongoRepository, MoviesMongoRepository MoviesMongoRepository) {
        this.BooksNeo4jRepository = BooksNeo4jRepository;
        this.MoviesNeo4jRepository = MoviesNeo4jRepository;
        this.BooksMongoRepository = BooksMongoRepository;
        this.MoviesMongoRepository = MoviesMongoRepository;
    }

    /* ================================ MEDIAS CRUD ================================ */

    public Slice<MediaAverageDto> browseMedia(MediaType mediaType, String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (mediaType == MediaType.Books) {
            return BooksMongoRepository.findByNameContaining(name, pageable);
        } else {
            return MoviesMongoRepository.findByNameContaining(name, pageable);
        }
    }

    public MediaDetailsDto getMediaById(MediaType mediaType, String mediaId) {
        MediaMongo media = mediaType == MediaType.Books ? BooksMongoRepository.findById(mediaId)
                .orElseThrow(() -> new NoSuchElementException("Media not found with id: " + mediaId)) :
                MoviesMongoRepository.findById(mediaId)
                        .orElseThrow(() -> new NoSuchElementException("Media not found with id: " + mediaId));

        if (mediaType == MediaType.Books) {
            BooksMongo Books = (BooksMongo) media;
            return BooksDetailsDto.builder()
                    .name(Books.getName())
                    .status(Books.getStatus())
                    .avgScore(Books.getNumScores() == 0 ? 0 : (double) Books.getSumScores() / Books.getNumScores())
                    .genres(Books.getGenres())
                    .synopsis(Books.getSynopsis())
                    .type(Books.getType())
                    .chapters(Books.getChapters())
                    .authors(Books.getAuthors())
                    .reviews(Books.getReviews())
                    .build();
        } else {
            MoviesMongo Movies = (MoviesMongo) media;
            return MoviesDetailsDto.builder()
                    .name(Movies.getName())
                    .status(Movies.getStatus())
                    .avgScore(Movies.getNumScores() == 0 ? 0 : (double) Movies.getSumScores() / Movies.getNumScores())
                    .genres(Movies.getGenres())
                    .synopsis(Movies.getSynopsis())
                    .type(Movies.getType())
                    .episodes(Movies.getEpisodes())
                    .source(Movies.getSource())
                    .duration(Movies.getDuration())
                    .studios(Movies.getStudios())
                    .reviews(Movies.getReviews())
                    .build();
        }
    }

    @Retryable(
            retryFor = TransactionSystemException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public String addMedia(MediaCreationDto mediaCreationDto) {
        String mediaId = UUID.randomUUID().toString();
        if (mediaCreationDto instanceof BooksCreationDto BooksCreationDto) {
            BooksNeo4j newBooksNeo4j = new BooksNeo4j();
            newBooksNeo4j.setId(mediaId);
            newBooksNeo4j.setName(BooksCreationDto.getName());
            newBooksNeo4j.setStatus(BooksCreationDto.getStatus());
            newBooksNeo4j.setChapters(BooksCreationDto.getChapters());
            newBooksNeo4j.setGenres(BooksCreationDto.getGenres());
            BooksNeo4jRepository.save(newBooksNeo4j);

            BooksMongo newBooksMongo = new BooksMongo();
            newBooksMongo.setId(mediaId);
            newBooksMongo.setName(BooksCreationDto.getName());
            newBooksMongo.setStatus(BooksCreationDto.getStatus());
            newBooksMongo.setChapters(BooksCreationDto.getChapters());
            newBooksMongo.setSumScores(0);
            newBooksMongo.setNumScores(0);
            newBooksMongo.setGenres(BooksCreationDto.getGenres());
            newBooksMongo.setType(BooksCreationDto.getType());
            newBooksMongo.setAuthors(BooksCreationDto.getAuthors());
            newBooksMongo.setSynopsis(BooksCreationDto.getSynopsis());
            BooksMongoRepository.save(newBooksMongo);

            return "Successfully added Books";
        } else if (mediaCreationDto instanceof MoviesCreationDto MoviesCreationDto) {
            MoviesNeo4j newMoviesNeo4j = new MoviesNeo4j();
            newMoviesNeo4j.setId(mediaId);
            newMoviesNeo4j.setName(MoviesCreationDto.getName());
            newMoviesNeo4j.setStatus(MoviesCreationDto.getStatus());
            newMoviesNeo4j.setEpisodes(MoviesCreationDto.getEpisodes());
            newMoviesNeo4j.setGenres(MoviesCreationDto.getGenres());
            MoviesNeo4jRepository.save(newMoviesNeo4j);

            MoviesMongo newMoviesMongo = new MoviesMongo();
            newMoviesMongo.setId(mediaId);
            newMoviesMongo.setName(MoviesCreationDto.getName());
            newMoviesMongo.setStatus(MoviesCreationDto.getStatus());
            newMoviesMongo.setEpisodes(MoviesCreationDto.getEpisodes());
            newMoviesMongo.setSumScores(0);
            newMoviesMongo.setNumScores(0);
            newMoviesMongo.setGenres(MoviesCreationDto.getGenres());
            newMoviesMongo.setType(MoviesCreationDto.getType());
            newMoviesMongo.setSource(MoviesCreationDto.getSource());
            newMoviesMongo.setDuration(MoviesCreationDto.getDuration());
            newMoviesMongo.setStudios(MoviesCreationDto.getStudios());
            newMoviesMongo.setSynopsis(MoviesCreationDto.getSynopsis());
            MoviesMongoRepository.save(newMoviesMongo);
            return "Successfully added Movies";
        }
        throw new IllegalArgumentException("Invalid media type");
    }

    @Retryable(
            retryFor = TransactionSystemException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public String updateMedia(String mediaId, MediaUpdateDto updates) {
        if (updates instanceof BooksUpdateDto BooksUpdateDto) {
            BooksMongo targetMongo = BooksMongoRepository.findById(mediaId)
                    .orElseThrow(() -> new NoSuchElementException("Media not found with id: " + mediaId));
            BooksNeo4j targetNeo4j = BooksNeo4jRepository.findById(mediaId)
                    .orElseThrow(() -> new NoSuchElementException("Media not found with id: " + mediaId));

            if (BooksUpdateDto.getName() != null) {
                targetMongo.setName(BooksUpdateDto.getName());
                targetNeo4j.setName(BooksUpdateDto.getName());
            }
            if (BooksUpdateDto.getStatus() != null) {
                targetMongo.setStatus(BooksUpdateDto.getStatus());
                targetNeo4j.setStatus(BooksUpdateDto.getStatus());
            }
            if (BooksUpdateDto.getChapters() != 0) {
                if(BooksUpdateDto.getChapters() < targetMongo.getChapters()) {
                    throw new IllegalArgumentException("Cannot decrease number of chapters");
                }
                targetMongo.setChapters(BooksUpdateDto.getChapters());
                targetNeo4j.setChapters(BooksUpdateDto.getChapters());
            }
            if (BooksUpdateDto.getGenres() != null) {
                targetMongo.setGenres(BooksUpdateDto.getGenres());
                targetNeo4j.setGenres(BooksUpdateDto.getGenres());
            }
            if (BooksUpdateDto.getAuthors() != null) {
                targetMongo.setAuthors(BooksUpdateDto.getAuthors());
            }
            if (BooksUpdateDto.getSynopsis() != null) {
                targetMongo.setSynopsis(BooksUpdateDto.getSynopsis());
            }
            if (BooksUpdateDto.getType() != null) {
                targetMongo.setType(BooksUpdateDto.getType());
            }
            BooksNeo4jRepository.save(targetNeo4j);
            BooksMongoRepository.save(targetMongo);

            return "Successfully updated media";
        } else if (updates instanceof MoviesUpdateDto MoviesUpdateDto) {
            MoviesMongo targetMongo = MoviesMongoRepository.findById(mediaId)
                    .orElseThrow(() -> new NoSuchElementException("Media not found with id: " + mediaId));
            MoviesNeo4j targetNeo4j = MoviesNeo4jRepository.findById(mediaId)
                    .orElseThrow(() -> new NoSuchElementException("Media not found with id: " + mediaId));

            if (MoviesUpdateDto.getName() != null) {
                targetMongo.setName(MoviesUpdateDto.getName());
                targetNeo4j.setName(MoviesUpdateDto.getName());
            }
            if (MoviesUpdateDto.getStatus() != null) {
                targetMongo.setStatus(MoviesUpdateDto.getStatus());
                targetNeo4j.setStatus(MoviesUpdateDto.getStatus());
            }
            if (MoviesUpdateDto.getEpisodes() != 0) {
                if(MoviesUpdateDto.getEpisodes() < targetMongo.getEpisodes()) {
                    throw new IllegalArgumentException("Cannot decrease number of episodes");
                }
                targetMongo.setEpisodes(MoviesUpdateDto.getEpisodes());
                targetNeo4j.setEpisodes(MoviesUpdateDto.getEpisodes());
            }
            if (MoviesUpdateDto.getGenres() != null) {
                targetMongo.setGenres(MoviesUpdateDto.getGenres());
                targetNeo4j.setGenres(MoviesUpdateDto.getGenres());
            }
            if (MoviesUpdateDto.getSynopsis() != null) {
                targetMongo.setSynopsis(MoviesUpdateDto.getSynopsis());
            }
            if (MoviesUpdateDto.getType() != null) {
                targetMongo.setType(MoviesUpdateDto.getType());
            }
            if (MoviesUpdateDto.getSource() != null) {
                targetMongo.setSource(MoviesUpdateDto.getSource());
            }
            if (MoviesUpdateDto.getDuration() != 0) {
                targetMongo.setDuration(MoviesUpdateDto.getDuration());
            }
            if (MoviesUpdateDto.getStudios() != null) {
                targetMongo.setStudios(MoviesUpdateDto.getStudios());
            }

            MoviesNeo4jRepository.save(targetNeo4j);
            MoviesMongoRepository.save(targetMongo);
            return "Successfully updated media";
        }
        throw new IllegalArgumentException("Invalid media type");
    }

    @Retryable(
            retryFor = TransactionSystemException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public String deleteMedia(String mediaId, MediaType mediaType) {
        if (mediaType == MediaType.Books) {
            BooksMongo targetMongo = BooksMongoRepository.findById(mediaId)
                    .orElseThrow(() -> new NoSuchElementException("Media not found with id: " + mediaId));
            BooksNeo4j targetNeo4j = BooksNeo4jRepository.findById(mediaId)
                    .orElseThrow(() -> new NoSuchElementException("Media not found with id: " + mediaId));
            BooksNeo4jRepository.delete(targetNeo4j);
            BooksMongoRepository.delete(targetMongo);
        } else {
            MoviesMongo targetMongo = MoviesMongoRepository.findById(mediaId)
                    .orElseThrow(() -> new NoSuchElementException("Media not found with id: " + mediaId));
            MoviesNeo4j targetNeo4j = MoviesNeo4jRepository.findById(mediaId)
                    .orElseThrow(() -> new NoSuchElementException("Media not found with id: " + mediaId));
            MoviesNeo4jRepository.delete(targetNeo4j);
            MoviesMongoRepository.delete(targetMongo);
        }
        return "Successfully deleted media";
    }

    /* ================================ RECENSIONI ================================ */

    public String addReview(MediaType mediaType, String mediaId, UserMongo user, AddReviewDto review) {
        boolean hasReviewed;
        MediaMongo targetMongo;
        if (mediaType == MediaType.Books) {
            targetMongo = BooksMongoRepository.findById(mediaId)
                    .orElseThrow(() -> new NoSuchElementException("Media not found with id: " + mediaId));
            hasReviewed = targetMongo.getReviews().stream()
                    .anyMatch(r -> r.getUserId().equals(user.getId()));
        } else {
            targetMongo = MoviesMongoRepository.findById(mediaId)
                    .orElseThrow(() -> new NoSuchElementException("Media not found with id: " + mediaId));
            hasReviewed = targetMongo.getReviews().stream()
                    .anyMatch(r -> r.getUserId().equals(user.getId()));
        }

        if (hasReviewed) {
            throw new IllegalArgumentException("User has already reviewed this media");
        }

        ReviewDto newReview = new ReviewDto();
        newReview.setUserId(user.getId());
        newReview.setUsername(user.getUsername());
        newReview.setScore(review.getScore());
        newReview.setComment(review.getComment());
        newReview.setTimestamp(new Date());

        List<ReviewDto> reviews = targetMongo.getReviews();
        reviews.add(newReview);
        targetMongo.setReviews(reviews);
        targetMongo.setSumScores(targetMongo.getSumScores() + review.getScore());
        targetMongo.setNumScores(targetMongo.getNumScores() + 1);

        if (mediaType == MediaType.Books) {
            BooksMongo BooksMongo = (BooksMongo) targetMongo;
            BooksMongoRepository.save(BooksMongo);
        } else {
            MoviesMongo MoviesMongo = (MoviesMongo) targetMongo;
            MoviesMongoRepository.save(MoviesMongo);
        }

        return "Successfully added review";
    }

    public String deleteReview(String mediaId, String reviewId, MediaType mediaType) {
        if (mediaType == MediaType.Books) {
            BooksMongo targetMongo = BooksMongoRepository.findById(mediaId)
                    .orElseThrow(() -> new NoSuchElementException("Media not found with id: " + mediaId));
            List<ReviewDto> reviews = targetMongo.getReviews();
            ReviewDto review = reviews.stream().filter(r -> r.getUserId().equals(reviewId))
                    .findFirst().orElseThrow(() -> new NoSuchElementException("Review not found with id: " + reviewId));
            reviews.remove(review);
            targetMongo.setReviews(reviews);
            targetMongo.setSumScores(targetMongo.getSumScores() - review.getScore());
            targetMongo.setNumScores(targetMongo.getNumScores() - 1);
            BooksMongoRepository.save(targetMongo);
        } else {
            MoviesMongo targetMongo = MoviesMongoRepository.findById(mediaId)
                    .orElseThrow(() -> new NoSuchElementException("Media not found with id: " + mediaId));
            List<ReviewDto> reviews = targetMongo.getReviews();
            ReviewDto review = reviews.stream().filter(r -> r.getUserId().equals(reviewId))
                    .findFirst().orElseThrow(() -> new NoSuchElementException("Review not found with id: " + reviewId));
            reviews.remove(review);
            targetMongo.setReviews(reviews);
            targetMongo.setSumScores(targetMongo.getSumScores() - review.getScore());
            targetMongo.setNumScores(targetMongo.getNumScores() - 1);
            MoviesMongoRepository.save(targetMongo);
        }
        return "Successfully deleted review";
    }
}