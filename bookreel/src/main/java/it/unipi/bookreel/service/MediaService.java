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
    private final FilmsNeo4jRepository FilmsNeo4jRepository;
    private final BooksMongoRepository BooksMongoRepository;
    private final FilmsMongoRepository FilmsMongoRepository;

    @Autowired
    public MediaService(BooksNeo4jRepository BooksNeo4jRepository, FilmsNeo4jRepository FilmsNeo4jRepository, BooksMongoRepository BooksMongoRepository, FilmsMongoRepository FilmsMongoRepository) {
        this.BooksNeo4jRepository = BooksNeo4jRepository;
        this.FilmsNeo4jRepository = FilmsNeo4jRepository;
        this.BooksMongoRepository = BooksMongoRepository;
        this.FilmsMongoRepository = FilmsMongoRepository;
    }

    /* ================================ MEDIAS CRUD ================================ */

    public Slice<MediaAverageDto> browseMedia(MediaType mediaType, String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (mediaType == MediaType.Books) {
            return BooksMongoRepository.findByNameContaining(name, pageable);
        } else {
            return FilmsMongoRepository.findByNameContaining(name, pageable);
        }
    }

    public MediaDetailsDto getMediaById(MediaType mediaType, String mediaId) {
        MediaMongo media = mediaType == MediaType.Books ? BooksMongoRepository.findById(mediaId)
                .orElseThrow(() -> new NoSuchElementException("Media not found with id: " + mediaId)) :
                FilmsMongoRepository.findById(mediaId)
                        .orElseThrow(() -> new NoSuchElementException("Media not found with id: " + mediaId));

        if (mediaType == MediaType.Books) {
            BooksMongo Books = (BooksMongo) media;
            return BooksDetailsDto.builder()
                    .name(Books.getName())
                    .avgScore(Books.getNumScores() == 0 ? 0 : (double) Books.getSumScores() / Books.getNumScores())
                    .genres(Books.getGenres())
                    .type(Books.getType())
                    .synopsis(Books.getSynopsis())
                    .reviews(Books.getReviews())
                    .numPages(Books.getNumPages())
                    .authors(Books.getAuthors())
                    .publishers(Books.getPublishers())
                    .build();
        } else {
            FilmsMongo Films = (FilmsMongo) media;
            return FilmsDetailsDto.builder()
                    .name(Films.getName())
                    .avgScore(Films.getNumScores() == 0 ? 0 : (double) Films.getSumScores() / Films.getNumScores())
                    .genres(Films.getGenres())
                    .type(Films.getType())
                    .synopsis(Films.getSynopsis())
                    .reviews(Films.getReviews())
                    .duration(Films.getDuration())
                    .studios(Films.getStudios())
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
            newBooksNeo4j.setGenres(BooksCreationDto.getGenres());
            BooksNeo4jRepository.save(newBooksNeo4j);

            BooksMongo newBooksMongo = new BooksMongo();
            newBooksMongo.setId(mediaId);
            newBooksMongo.setName(BooksCreationDto.getName());
            newBooksMongo.setSumScores(0);
            newBooksMongo.setNumScores(0);
            newBooksMongo.setGenres(BooksCreationDto.getGenres());
            newBooksMongo.setType(BooksCreationDto.getType());
            newBooksMongo.setNumPages(BooksCreationDto.getNumPages());
            newBooksMongo.setAuthors(BooksCreationDto.getAuthors());
            newBooksMongo.setPublishers(BooksCreationDto.getPublishers());
            newBooksMongo.setSynopsis(BooksCreationDto.getSynopsis());
            BooksMongoRepository.save(newBooksMongo);

            return "Successfully added Books";
        } else if (mediaCreationDto instanceof FilmsCreationDto FilmsCreationDto) {
            FilmsNeo4j newFilmsNeo4j = new FilmsNeo4j();
            newFilmsNeo4j.setId(mediaId);
            newFilmsNeo4j.setName(FilmsCreationDto.getName());
            newFilmsNeo4j.setGenres(FilmsCreationDto.getGenres());
            FilmsNeo4jRepository.save(newFilmsNeo4j);

            FilmsMongo newFilmsMongo = new FilmsMongo();
            newFilmsMongo.setId(mediaId);
            newFilmsMongo.setName(FilmsCreationDto.getName());
            newFilmsMongo.setSumScores(0);
            newFilmsMongo.setNumScores(0);
            newFilmsMongo.setGenres(FilmsCreationDto.getGenres());
            newFilmsMongo.setType(FilmsCreationDto.getType());
            newFilmsMongo.setDuration(FilmsCreationDto.getDuration());
            newFilmsMongo.setStudios(FilmsCreationDto.getStudios());
            newFilmsMongo.setSynopsis(FilmsCreationDto.getSynopsis());
            FilmsMongoRepository.save(newFilmsMongo);
            return "Successfully added Films";
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
            if (BooksUpdateDto.getGenres() != null) {
                targetMongo.setGenres(BooksUpdateDto.getGenres());
                targetNeo4j.setGenres(BooksUpdateDto.getGenres());
            }
            if (BooksUpdateDto.getAuthors() != null) {
                targetMongo.setAuthors(BooksUpdateDto.getAuthors());
            }
            if (BooksUpdateDto.getNumPages() != null) {
                targetMongo.setAuthors(BooksUpdateDto.getNumPages());
            }
            if (BooksUpdateDto.getPublishers() != null) {
                targetMongo.setAuthors(BooksUpdateDto.getPublishers());
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
        } else if (updates instanceof FilmsUpdateDto FilmsUpdateDto) {
            FilmsMongo targetMongo = FilmsMongoRepository.findById(mediaId)
                    .orElseThrow(() -> new NoSuchElementException("Media not found with id: " + mediaId));
            FilmsNeo4j targetNeo4j = FilmsNeo4jRepository.findById(mediaId)
                    .orElseThrow(() -> new NoSuchElementException("Media not found with id: " + mediaId));

            if (FilmsUpdateDto.getName() != null) {
                targetMongo.setName(FilmsUpdateDto.getName());
                targetNeo4j.setName(FilmsUpdateDto.getName());
            }
            if (FilmsUpdateDto.getGenres() != null) {
                targetMongo.setGenres(FilmsUpdateDto.getGenres());
                targetNeo4j.setGenres(FilmsUpdateDto.getGenres());
            }
            if (FilmsUpdateDto.getSynopsis() != null) {
                targetMongo.setSynopsis(FilmsUpdateDto.getSynopsis());
            }
            if (FilmsUpdateDto.getType() != null) {
                targetMongo.setType(FilmsUpdateDto.getType());
            }
            if (FilmsUpdateDto.getDuration() != 0) {
                targetMongo.setDuration(FilmsUpdateDto.getDuration());
            }
            if (FilmsUpdateDto.getStudios() != null) {
                targetMongo.setStudios(FilmsUpdateDto.getStudios());
            }

            FilmsNeo4jRepository.save(targetNeo4j);
            FilmsMongoRepository.save(targetMongo);
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
            FilmsMongo targetMongo = FilmsMongoRepository.findById(mediaId)
                    .orElseThrow(() -> new NoSuchElementException("Media not found with id: " + mediaId));
            FilmsNeo4j targetNeo4j = FilmsNeo4jRepository.findById(mediaId)
                    .orElseThrow(() -> new NoSuchElementException("Media not found with id: " + mediaId));
            FilmsNeo4jRepository.delete(targetNeo4j);
            FilmsMongoRepository.delete(targetMongo);
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
            targetMongo = FilmsMongoRepository.findById(mediaId)
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
            FilmsMongo FilmsMongo = (FilmsMongo) targetMongo;
            FilmsMongoRepository.save(FilmsMongo);
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
            FilmsMongo targetMongo = FilmsMongoRepository.findById(mediaId)
                    .orElseThrow(() -> new NoSuchElementException("Media not found with id: " + mediaId));
            List<ReviewDto> reviews = targetMongo.getReviews();
            ReviewDto review = reviews.stream().filter(r -> r.getUserId().equals(reviewId))
                    .findFirst().orElseThrow(() -> new NoSuchElementException("Review not found with id: " + reviewId));
            reviews.remove(review);
            targetMongo.setReviews(reviews);
            targetMongo.setSumScores(targetMongo.getSumScores() - review.getScore());
            targetMongo.setNumScores(targetMongo.getNumScores() - 1);
            FilmsMongoRepository.save(targetMongo);
        }
        return "Successfully deleted review";
    }
}