package it.unipi.bookreel.DTO.media;

import java.util.List;

public record MediaListsDto (
     List<ListElementDto> plannedList,
     List<ListElementDto> inProgressList,
     List<ListElementDto> completedList
) {}