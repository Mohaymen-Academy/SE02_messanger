package com.mohaymen.service;

import com.mohaymen.model.entity.Block;
import com.mohaymen.model.entity.Profile;
import com.mohaymen.model.supplies.ChatType;
import com.mohaymen.model.supplies.ProfilePareId;
import com.mohaymen.repository.BlockRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class BlockService {

    private final BlockRepository blockRepository;

    private final ProfileService profileService;

    public BlockService(BlockRepository blockRepository, ProfileService profileService) {
        this.blockRepository = blockRepository;
        this.profileService = profileService;
    }

    public void blockUser(Long userId, Long block_id) throws Exception {
        Profile blocker = profileService.getProfile(userId);
        if (userId.equals(block_id))
            throw new Exception("You can not block yourself!");
        Profile blocked = profileService.getProfile(block_id);
        if (blocked.getType() != ChatType.USER)
            throw new Exception("Blocking a gp/channel is not available");
        ProfilePareId ppId = new ProfilePareId(blocker, blocked);
        Optional<Block> optionalBlock = blockRepository.findById(ppId);
        if (optionalBlock.isPresent())
            throw new Exception("you already blocked this user:|");
        blockRepository.save(new Block(blocker, blocked));
    }

    public void unblockUser(Long userId, long blocked_id) throws Exception {
        Profile blocker = profileService.getProfile(userId);
        if (userId.equals(blocked_id))
            throw new Exception("You can not unblock yourself!");
        Profile blocked = profileService.getProfile(blocked_id);
        if (blocked.getType() != ChatType.USER)
            throw new Exception("Unblocking a gp/channel is not available");
        ProfilePareId ppId = new ProfilePareId(blocker, blocked);
        Optional<Block> optionalBlock = blockRepository.findById(ppId);
        if (optionalBlock.isEmpty())
            throw new Exception(":|...");
        blockRepository.delete(optionalBlock.get());
    }

}
