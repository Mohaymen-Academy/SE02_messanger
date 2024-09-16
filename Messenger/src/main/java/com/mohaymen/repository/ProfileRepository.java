package com.mohaymen.repository;

import com.mohaymen.model.supplies.ChatType;
import com.mohaymen.model.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByTypeAndHandle(ChatType type, String handle);

    boolean existsByHandleAndProfileIDNot(String handle, Long id);

    @Transactional
    @Query(value = "INSERT INTO profile (is_deleted, type, profile_id,profile_name, handle) VALUES (:is_deleted, :type, :profile_id,:profile_name, :handle) RETURNING *",nativeQuery = true)
    Profile insertProfile(@Param("is_deleted") boolean isDeleted,
                          @Param("type") int type,
                          @Param("profile_id") Long profileId,
                          @Param("profile_name") String profileName,
                          @Param("handle") String handle
    );

}
