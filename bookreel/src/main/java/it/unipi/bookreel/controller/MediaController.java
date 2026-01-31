package it.unipi.bookreel.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import it.unipi.bookreel.DTO.media.AddReviewDto;
import it.unipi.bookreel.DTO.media.MediaAverageDto;
import it.unipi.bookreel.DTO.media.MediaDetailsDto;
import it.unipi.bookreel.DTO.media.MediaIdNameDto;
import it.unipi.bookreel.enumerator.MediaType;
import it.unipi.bookreel.model.UserPrincipal;
import it.unipi.bookreel.service.MediaService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/media")
@Tag(name = "Media management", description = "Operations related to media catalog")
public class MediaController {

    private final MediaService mediaService;

    @Autowired
    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @GetMapping("/{mediaType}")
    public ResponseEntity<?> browseMedia(
            @PathVariable MediaType mediaType,
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(0) @Max(100) int size) {
        Slice<MediaAverageDto> results = mediaService.browseMedia(mediaType, name, size);
        if (results.isEmpty()) {
            return ResponseEntity.ok("No media found with this name");
        } else
            return ResponseEntity.ok(results);
    }

    @GetMapping("/{mediaType}/{mediaId}")
    public ResponseEntity<MediaDetailsDto> getMediaById(@PathVariable MediaType mediaType, @PathVariable String mediaId) {
        return ResponseEntity.ok(mediaService.getMediaById(mediaType, mediaId));
    }

    @PostMapping("/{mediaType}/{mediaId}/review")
    public ResponseEntity<String> addReview(@PathVariable MediaType mediaType, @PathVariable String mediaId, @RequestBody AddReviewDto review) {
        UserPrincipal user = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(mediaService.addReview(mediaType, mediaId, user.getUser(), review));
    }

    @DeleteMapping("/{mediaType}/{mediaId}/review/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable MediaType mediaType, @PathVariable String mediaId, @PathVariable String reviewId) {
        UserPrincipal user = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.getUser().getId().equals(reviewId)) {
            return ResponseEntity.badRequest().body("You can delete only your reviews");
        }
        return ResponseEntity.ok(mediaService.deleteReview(mediaId, reviewId, mediaType));
    }
}