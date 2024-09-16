package com.mohaymen.repository;

import com.mohaymen.model.entity.Update;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface UpdateRepository extends JpaRepository<Update, Long> {

    @Query(value = "select update.* from (SELECT type, message_id, MAX(id) \"id\" " +
            "FROM update WHERE chat_id = :chatId " +
            "AND id > :id Group by (type, message_id)) t " +
            "join update on update.id = t.id", nativeQuery = true)
    List<Update> findByChatIdAndIdGreaterThan(@Param("chatId") String chatId, @Param("id") Long id);

    Update findTopByChatIdOrderByIdDesc(String chatId);

}
