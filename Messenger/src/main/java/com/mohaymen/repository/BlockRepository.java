package com.mohaymen.repository;

import com.mohaymen.model.entity.Block;
import com.mohaymen.model.supplies.ProfilePareId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockRepository extends JpaRepository<Block, ProfilePareId> {
}
