package it.unipi.bookreel.repository;

import it.unipi.bookreel.model.MonthAnalytic;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonthAnalyticRepository extends MongoRepository<MonthAnalytic, String> {
    MonthAnalytic findTopByOrderByYearDesc();
    List<MonthAnalytic> findAllByOrderByYear();
}