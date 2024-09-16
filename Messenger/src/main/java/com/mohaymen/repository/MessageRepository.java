package com.mohaymen.repository;

import com.mohaymen.model.entity.Message;
import com.mohaymen.model.entity.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByReceiverAndMessageIDLessThanOrderByTimeDesc(Profile receiver,
                                                            Long messageID,
                                                            Pageable pageable);

    List<Message> findByReceiverAndMessageIDGreaterThanOrderByTimeDesc(Profile receiver,
                                                               Long messageID,
                                                               Pageable pageable);

    List<Message> findAllBySenderAndReceiver(Profile senderId, Profile receiverId);
    List<Message> findTopNByReceiverAndMessageIDOrderByTimeDesc(Profile receiver,
                                                        Long messageID,
                                                        Pageable pageable,
                                                        int limit);

    @Query("Select m from Message m where ((m.sender = :sender And m.receiver = :receiver) " +
            "OR (m.sender = :receiver And m.receiver = :sender)) And (m.messageID < :messageID) " +
            "ORDER BY m.messageID DESC " +
            "LIMIT :limit")
    List<Message> findPVUpMessages(@Param("sender") Profile sender,
                                   @Param("receiver") Profile receiver,
                                   @Param("messageID") Long messageID,
                                   @Param("limit") int limit);

    @Query("Select m from Message m where ((m.sender = :sender And m.receiver = :receiver) " +
            "OR (m.sender = :receiver And m.receiver = :sender)) And (m.messageID > :messageID) " +
            "ORDER BY m.messageID ASC " +
            "LIMIT :limit")
    List<Message> findPVDownMessages(@Param("sender") Profile sender,
                                     @Param("receiver") Profile receiver,
                                     @Param("messageID") Long messageID,
                                     @Param("limit") int limit);

    @Query("Select m from Message m where ((m.sender = :sender AND m.receiver = :receiver) " +
            "OR (m.sender = :receiver AND m.receiver = :sender)) " +
            "ORDER BY m.messageID DESC " +
            "LIMIT :limit")
    List<Message> findPVTopNMessages(@Param("sender") Profile sender,
                                     @Param("receiver") Profile receiver,
                                     @Param("limit") int limit);

    int countByReceiverAndMessageIDGreaterThan(Profile receiver, Long messageID);

    int countBySenderAndReceiverAndMessageIDGreaterThan(Profile sender, Profile receiver, Long messageID);

    Message findTopByReceiverOrderByMessageIDDesc(Profile receiver);

    @Query("Select m from Message m where ((m.sender = :sender AND m.receiver = :receiver) " +
            "OR (m.sender = :receiver AND m.receiver = :sender)) " +
            "ORDER BY m.messageID ASC " +
            "LIMIT 1")
    Message findPVFirstMessage(@Param("sender") Profile sender, @Param("receiver") Profile receiver);

    Message findFirstByReceiver(Profile receiver);

    @Query("Select m from Message m where ((m.sender = :sender AND m.receiver = :receiver) " +
            "OR (m.sender = :receiver AND m.receiver = :sender)) " +
            "AND m.messageID >= :minMessageID AND m.messageID <= :maxMessageID")
    List<Message> findMessagesInRange(@Param("sender") Profile sender,
                                      @Param("receiver") Profile receiver,
                                      @Param("minMessageID") Long minMessageID,
                                      @Param("maxMessageID") Long maxMessageID);

    List<Message> findByReceiverAndMessageIDGreaterThanEqualAndMessageIDLessThanEqual(Profile receiver,
                                                                                      Long minMessageIDAmount,
                                                                                      Long maxMessageIDAmount);

    List<Message> findByReplyMessageId(Long replyMessageId);

    @Query("Select m from Message m where ((m.sender = :sender AND m.receiver = :receiver) " +
            "OR (m.sender = :receiver AND m.receiver = :sender)) " +
            "and m.media is not null and (m.media.contentType like :mediaType or m.media.contentType like :mediaType2)")
    List<Message> findAudioOrMediaOfPVChat(@Param("sender") Profile sender,
                                           @Param("receiver") Profile receiver,
                                           @Param("mediaType") String mediaType,
                                           @Param("mediaType2") String mediaType2);

    @Query("Select m from Message m where m.receiver = :receiver " +
            "and m.media is not null and (m.media.contentType like :mediaType or m.media.contentType like :mediaType2)")
    List<Message> findAudioOrMediaOfChannelOrGroup(@Param("receiver") Profile receiver,
                                                   @Param("mediaType") String mediaType,
                                                   @Param("mediaType2") String mediaType2);

    @Query("Select m from Message m where ((m.sender = :sender AND m.receiver = :receiver) " +
            "OR (m.sender = :receiver AND m.receiver = :sender)) " +
            "and m.media is not null and m.media.contentType not like 'image%' " +
            "and m.media.contentType not like 'ogg%' " +
            "and m.media.contentType not like 'mp3%' and m.media.contentType not like 'video%'")
    List<Message> findFilesOfPVChat(@Param("sender") Profile sender,
                                    @Param("receiver") Profile receiver);

    @Query("Select m from Message m where m.receiver = :receiver and m.media is not null " +
            "and m.media.contentType not like 'image%' " +
            "and m.media.contentType not like 'ogg%'" +
            "and m.media.contentType not like 'mp3%' and m.media.contentType not like 'video%'")
    List<Message> findFilesOfChannelOrGroup(@Param("receiver") Profile receiver);

}
