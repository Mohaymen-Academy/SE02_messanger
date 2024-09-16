package com.mohaymen.service;

import com.mohaymen.model.entity.*;
import com.mohaymen.model.json_item.*;
import com.mohaymen.model.supplies.*;
import com.mohaymen.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChatService {

    private final ChatParticipantRepository cpRepository;

    private final ProfileRepository profileRepository;

    private final MessageRepository messageRepository;

    private final MessageSeenRepository msRepository;

    private final ProfileService profileService;

    private final BlockRepository blockRepository;

    private final UpdateRepository updateRepository;

    private final AccountService accountService;

    private final ServerService serverService;

    private final SearchService searchService;

    private final MessageService messageService;

    private final ChatParticipantService cpService;

    private final ContactService contactService;

    private final LogService logger;

    public ChatService(ChatParticipantRepository cpRepository,
                       ProfileRepository profileRepository,
                       MessageRepository messageRepository,
                       MessageSeenRepository msRepository,
                       ProfileService profileService, BlockRepository blockRepository,
                       UpdateRepository updateRepository,
                       AccountService accountService,
                       ServerService serverService,
                       SearchService searchService,
                       MessageService messageService,
                       ChatParticipantService cpService,
                       ContactService contactService,
                       LogRepository logRepository) {
        this.cpRepository = cpRepository;
        this.profileRepository = profileRepository;
        this.messageRepository = messageRepository;
        this.msRepository = msRepository;
        this.profileService = profileService;
        this.blockRepository = blockRepository;
        this.updateRepository = updateRepository;
        this.accountService = accountService;
        this.serverService = serverService;
        this.searchService = searchService;
        this.messageService = messageService;
        this.cpService = cpService;
        this.contactService = contactService;
        this.logger = new LogService(logRepository, ChatService.class.getName());
    }

    private void setSavedMessageInfo(Profile profile) {
        profile.setProfileName("Saved Message");
        profile.setDefaultProfileColor("#2ee6ca");
        profile.setLastProfilePicture(null);
    }

    private void setProfileInfoToGetChats(Profile profile, Profile user, boolean hasBlockedYou) {
        profile.setProfileName(contactService.getProfileWithCustomName(user, profile).getProfileName());
        profile.setAccessPermission(profileService.getAccessPermission(user, profile));
        if (hasBlockedYou) profile.setLastProfilePicture(null);
        if (profile.getProfileID().equals(user.getProfileID())) setSavedMessageInfo(profile);
        profile.setStatus(hasBlockedYou ? "Last seen a long time ago"
                : accountService.getLastSeen(profile.getProfileID()));
    }

    private Message getLastMessage(Profile user, Profile profile) {
        if (profile.getType() == ChatType.USER)
            return messageRepository.findPVTopNMessages(user, profile, 1).get(0);
        else
            return messageRepository.findTopByReceiverOrderByMessageIDDesc(profile);
    }

    private long getLastMessageId(Profile user, Profile profile) {
        ProfilePareId profilePareId = new ProfilePareId(user, profile);
        Optional<MessageSeen> messageSeenOptional = msRepository.findById(profilePareId);
        if (messageSeenOptional.isEmpty()) return 0;
        else return messageSeenOptional.get().getLastMessageSeenId();
    }

    private int getUnreadMessageCount(Profile user, Profile profile, Long messageId) {
        if (profile.getType() == ChatType.USER)
            return messageRepository.countBySenderAndReceiverAndMessageIDGreaterThan(profile, user, messageId);
        else
            return messageRepository.countByReceiverAndMessageIDGreaterThan(profile, messageId);
    }

    private List<Update> getUpdates(ChatParticipant p) throws Exception {
        Long lastUpdate = p.getLastUpdate() != null ? p.getLastUpdate() : 0;
        List<Update> updates = updateRepository.findByChatIdAndIdGreaterThan(p.getChatId(), lastUpdate);
        for (Update u : updates)
            if (u.getUpdateType().equals(UpdateType.EDIT) || u.getUpdateType().equals(UpdateType.SEEN))
                u.setMessage(messageService.getSingleMessage(u.getMessageId()));
        return updates;
    }

    private ChatDisplay createChatDisplay(Profile profile, Profile user,
                                          ChatParticipant p, boolean hasBlockedYou) throws Exception {
        return ChatDisplay.builder()
                .profile(profile)
                .lastMessage(getLastMessage(user, profile))
                .unreadMessageCount(getUnreadMessageCount(user, profile, getLastMessageId(user, profile)))
                .updates(getUpdates(p))
                .isPinned(p.isPinned())
                .hasBlockedYou(hasBlockedYou)
                .build();
    }

    private void sortChats(List<ChatDisplay> chats, Long userId) {
        try {
            List<ChatDisplay> pinnedChats = chats.stream()
                    .filter(ChatDisplay::isPinned)
                    .sorted(Comparator.comparing(x -> x.getLastMessage().getMessageID(), Comparator.reverseOrder()))
                    .toList();
            List<ChatDisplay> unpinnedChats = chats.stream()
                    .filter(x -> !x.isPinned())
                    .sorted(Comparator.comparing(x -> x.getLastMessage().getMessageID(), Comparator.reverseOrder()))
                    .toList();
            chats.clear();
            chats.addAll(pinnedChats);
            chats.addAll(unpinnedChats);
        } catch (Exception e) {
            logger.info("Cannot sort chats for user with id: " + userId);
        }
    }

    public ChatListInfo getChats(Long userId, int limit) throws Exception {
        Profile user = getProfile(userId);
        accountService.UpdateLastSeen(userId);
        List<ChatDisplay> chats = new ArrayList<>();
        for (ChatParticipant p : cpRepository.findByUser(user)) {
            Profile profile = p.getDestination();
            boolean hasBlockedYou = blockRepository.findById(new ProfilePareId(profile, user)).isPresent();
            setProfileInfoToGetChats(profile, user, hasBlockedYou);
            chats.add(createChatDisplay(profile, user, p, hasBlockedYou));
        }
        sortChats(chats, userId);
        return chats.size() > limit
                ? new ChatListInfo(chats.subList(0, limit), false)
                : new ChatListInfo(chats, true);
    }

    private String createRandomHandle(ChatType type) {
        UUID uuid = UUID.randomUUID();
        Optional<Profile> profile = profileRepository.findByTypeAndHandle(type, uuid.toString());
        if (profile.isPresent()) return createRandomHandle(type);
        return uuid.toString();
    }

    @Transactional
    public Long createChat(Long userId, String name, ChatType type,
                           String bio, List<Long> members) throws Exception {
        if (type.equals(ChatType.USER)) throw new Exception("type is not valid.");
        Profile chat = new Profile();
        chat.setProfileName(name);
        chat.setType(type);
        chat.setBiography(bio);
        chat.setHandle(createRandomHandle(type));
        chat.setDefaultProfileColor(AccessService.generateColor(chat.getHandle()));
        chat.setMemberCount(0);
        profileRepository.save(chat);
        if (type.equals(ChatType.CHANNEL))
            searchService.addChannel(chat);
        cpService.createChatParticipant(getProfile(userId), chat, true);
        for (Number memberId : members)
            cpService.createChatParticipant(getProfile(memberId.longValue()), chat, false);
        String typeName = type.value == 1 ? "گروه" : "کانال";
        serverService.sendMessage(typeName + " ساخته شد", chat);
        return chat.getProfileID();
    }

    public void addMember(Long userId, Long chatId, Long memberId) throws Exception {
        Profile user = getProfile(userId);
        Profile chat = getProfile(chatId);
        if (!getParticipant(user, chat).isAdmin()) throw new Exception("Only admins can add members.");
        if (blockRepository.findById(new ProfilePareId(getProfile(memberId), user)).isPresent())
            throw new Exception("This user has blocked you, you can not add him/her to this chat");
        Profile newMember = getProfile(memberId);
        if (chat.getType().equals(ChatType.GROUP))
            serverService.sendMessage(newMember.getProfileName() + " در این گروه عضو شد", chat);
    }

    public void joinChannel(Long userId, Long chatId) throws Exception {
        cpService.createChatParticipant(getProfile(userId), getProfile(chatId), false);
    }

    public void addAdmin(Long userId, Long chatId, Long memberId) throws Exception {
        Profile user = getProfile(userId);
        Profile chat = getProfile(chatId);
        if (!getParticipant(user, chat).isAdmin()) throw new Exception("Only admins can add another admin.");
        Profile newAdmin = getProfile(memberId);
        if (blockRepository.findById(new ProfilePareId(newAdmin, user)).isPresent())
            throw new Exception("this user has blocked you");
        ChatParticipant chatParticipant = getParticipant(newAdmin, chat);
        chatParticipant.setAdmin(true);
        cpRepository.save(chatParticipant);
    }

    public void leaveChat(Long userId, Long chatId) throws Exception {
        Profile user = getProfile(userId);
        Profile chat = getProfile(chatId);
        ChatParticipant participant = getParticipant(user, chat);
        cpRepository.delete(participant);
        chat.setMemberCount(chat.getMemberCount() - 1);
        profileRepository.save(chat);
        if (chat.getType() == ChatType.GROUP)
            serverService.sendMessage(user.getProfileName() + " از گروه خارج شد", chat);
    }

    @Transactional
    public void deleteChat(Long userId, Long chatId) throws Exception {
        Profile user = getProfile(userId);
        Profile chat = getProfile(chatId);
        ChatParticipant chatParticipant = getParticipant(user, chat);

        ChatParticipant chatParticipant2 = null;

        if (chat.getType() == ChatType.USER) {
            List<Message> messages1 = messageRepository.findAllBySenderAndReceiver(user, chat);
            List<Message> messages2 = messageRepository.findAllBySenderAndReceiver(chat, user);
            for (Message msg : messages1)
                messageService.deleteMessage(user.getProfileID(),msg.getMessageID());
            for (Message msg : messages2)
                messageService.deleteMessage(chat.getProfileID(),msg.getMessageID());
//            messageRepository.deleteBySenderAndReceiver(user, chat);
//            messageRepository.deleteBySenderAndReceiver(chat, user);
//            chatParticipant2 = getParticipant(chat, user);
//            cpRepository.delete(chatParticipant);
//            cpRepository.delete(chatParticipant2);

        } else if (chatParticipant.isAdmin()) {
            if (chat.getType() == ChatType.CHANNEL)
                searchService.deleteChannel(chat);
            else
                serverService.sendMessage(user.getProfileName() + " (ادمین) از گروه خارج شد", chat);
            cpRepository.deleteByDestination(chat);
            accountService.deleteProfile(chat);
        } else {
            cpRepository.delete(chatParticipant);
            chat.setMemberCount(chat.getMemberCount() - 1);
            profileRepository.save(chat);
            if (chat.getType() == ChatType.GROUP)
                serverService.sendMessage(user.getProfileName() + " از گروه خارج شد", chat);
        }
    }

    public List<Profile> getMembers(Long userId, Long chatId) throws Exception {
        Profile user = getProfile(userId);
        Profile chat = getProfile(chatId);
        if (chat.getType().equals(ChatType.CHANNEL))
            if (getParticipant(user, chat).isAdmin())
                throw new Exception("You do not have permission to see members.");
        return cpRepository.findByDestination(chat).
                stream().map(ChatParticipant::getUser)
                .peek(p -> p.setStatus(accountService.getLastSeen(p.getProfileID()))).toList();
    }

    public boolean isMemberOfChannel(Long userId, Long chatId) throws Exception {
        return cpRepository.findById(new ProfilePareId(getProfile(userId), getProfile(chatId))).isPresent();
    }

    public ChatParticipant getParticipant(Profile user, Profile dest) throws Exception {
        Optional<ChatParticipant> participant = cpRepository.findById(new ProfilePareId(user, dest));
        if (participant.isEmpty())
            throw new Exception("user is not a member of this chat");
        return participant.get();
    }

    private Profile getProfile(Long profileId) throws Exception {
        Optional<Profile> optionalProfile = profileRepository.findById(profileId);
        if (optionalProfile.isEmpty()) throw new Exception("profile not found!");
        return optionalProfile.get();
    }

}
