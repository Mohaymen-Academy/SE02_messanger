package com.mohaymen.repository;

import com.mohaymen.model.entity.Account;
import com.mohaymen.model.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Profile> {

    Optional<Account> findByEmail(String email);

    Optional<Account>findById(Long id);

    Optional<Account> findByProfile(Profile profile);

}
