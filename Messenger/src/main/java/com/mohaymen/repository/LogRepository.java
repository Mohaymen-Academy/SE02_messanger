package com.mohaymen.repository;

import com.mohaymen.model.entity.Log;
import com.mohaymen.model.entity.Message;
import com.mohaymen.model.entity.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogRepository extends JpaRepository<Log, Long> {

    List<Log> findAllByOrderByIdDesc(Pageable pageable);
    List<Log> findByLoggerOrderByIdDesc(String logger, Pageable pageable);

}
