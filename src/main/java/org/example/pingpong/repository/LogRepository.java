package org.example.pingpong.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository extends ReactiveMongoRepository<LogEntry, String> {
}
