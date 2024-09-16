package com.mohaymen.repository;

import com.mohaymen.model.entity.*;
import com.mohaymen.model.supplies.ProfilePareId;
import com.mohaymen.model.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, ProfilePareId> {

    List<ChatParticipant> findByUser(Profile user);
    void deleteByDestination(Profile destination);

    List<ChatParticipant> findByDestination(Profile destination);
    @Modifying
    @Query("UPDATE ChatParticipant ch SET ch.pinnedMessage = null WHERE (ch.destination = :destination AND ch.pinnedMessage = :message)")
    void updateMessageIdByProfileDestinationAndMessageId(@Param("destination") Profile destination,
                                                         @Param("message") Message message);
    List<ChatParticipant> findByPinnedMessageAndDestination(Message message,Profile destination);

    List<ChatParticipant> findByChatId(String chatId);

    ChatParticipant findByDestinationAndUser(Profile destination, Profile user);

}
