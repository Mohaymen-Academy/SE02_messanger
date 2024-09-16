package com.mohaymen.service;

import com.mohaymen.full_text_search.*;
import com.mohaymen.model.entity.*;
import com.mohaymen.model.json_item.SearchResultItem;
import com.mohaymen.model.json_item.SearchResultItemGroup;
import com.mohaymen.model.supplies.ChatType;
import com.mohaymen.model.supplies.ProfilePareId;
import com.mohaymen.repository.*;
import org.apache.lucene.document.Document;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SearchService {
    private final AccountRepository accountRepository;

    private final BlockRepository blockRepository;
    private final MessageRepository messageRepository;

    private final ChatParticipantRepository chatParticipantRepository;

    private final ProfileRepository profileRepository;

    private final MessageSearch messageSearch;

    private final ChannelSearch channelSearch;

    private final UserSearch userSearch;

    public SearchService(AccountRepository accountRepository, BlockRepository blockRepository, MessageRepository messageRepository, ChatParticipantRepository chatParticipantRepository, ProfileRepository profileRepository) {
        this.accountRepository = accountRepository;
        this.blockRepository = blockRepository;
        this.messageRepository = messageRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.profileRepository = profileRepository;
        messageSearch = new MessageSearch();
        channelSearch = new ChannelSearch();
        userSearch = new UserSearch();
    }

    public void addMessage(Message message) {
        messageSearch.indexMessageDocument(message.getMessageID().toString(),
                message.getSender().getProfileID().toString(),
                message.getReceiver().getProfileID().toString(),
                message.getText());
    }

    public void updateMessage(Message message) {
        messageSearch.updateMessage(message.getMessageID().toString(),
                message.getSender().getProfileID().toString(),
                message.getReceiver().getProfileID().toString(),
                message.getText());
    }

    public void deleteMessage(Message message) {
        messageSearch.deleteMessage(message.getMessageID().toString());
    }

    public List<Message> searchInPv(Long senderId, Long receiverId, String searchEntry) {
        List<Document> documents = messageSearch.searchInPv(senderId.toString(),
                receiverId.toString(),
                searchEntry);
        return getMessagesListFromDocuments(documents);
    }

    public List<Message> searchInChat(Long receiverId, String searchEntry) {
        List<Document> documents = messageSearch.searchInChat(receiverId.toString(), searchEntry);
        return getMessagesListFromDocuments(documents);
    }

    public List<Message> searchInAllMessages(Long profileId, String searchEntry) {
        Optional<Profile> profile = profileRepository.findById(profileId);
        if (profile.isEmpty())
            return new ArrayList<>();
        Profile p = profile.get();
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByUser(p);
        List<String> receiverPvIds = new ArrayList<>();
        List<String> receiverChatIds = new ArrayList<>();
        for (ChatParticipant chatParticipant : chatParticipants) {
            if (chatParticipant.getDestination().getType() == ChatType.USER) {
                receiverPvIds.add(chatParticipant.getDestination().getProfileID().toString());
            } else {
                receiverChatIds.add(chatParticipant.getDestination().getProfileID().toString());
            }
        }
        List<Document> documents = messageSearch.searchInAllMessages(profileId.toString(),
                receiverPvIds,
                receiverChatIds,
                searchEntry);
        return getMessagesListFromDocuments(documents);
    }

    private List<Message> getMessagesListFromDocuments(List<Document> documents) {
        List<Message> messages = new ArrayList<>();
        for (Document d : documents) {
            Optional<Message> message = messageRepository.findById(Long.valueOf(d.get(FieldNameLucene.MESSAGE_iD)));
            message.ifPresent(messages::add);
        }
        return messages;
    }

    public void addChannel(Profile profile) {
        channelSearch.indexChannelDocument(profile.getProfileID().toString(),
                profile.getProfileName());
    }

    public void updateChannel(Profile profile) {
        channelSearch.updateChannel(profile.getProfileID().toString(),
                profile.getProfileName());
    }

    public void deleteChannel(Profile profile) {
        channelSearch.deleteChannel(profile.getProfileID().toString());
    }

    public List<Profile> searchInChannels(String searchEntry) {
        List<Document> documents = channelSearch.searchInAllChannels(searchEntry);
        return getProfilesFromDocuments(documents);
    }

    public void addUser(Account account) {
        userSearch.indexUserDocument(account.getProfile().getProfileID().toString(),
                account.getEmail(),
                account.getProfile().getHandle());
    }

    public void updateUser(Profile profile) {
        userSearch.updateUser(profile.getProfileID().toString(),
                profile.getHandle());
    }

    public void deleteUser(Profile profile) {
        userSearch.deleteUser(profile.getProfileID().toString());
    }

    public List<Profile> searchInUsers(String searchEntry) {
        List<Document> documents = userSearch.searchInAllUsers(searchEntry);
        return getProfilesFromDocuments(documents);
    }

    private List<Profile> getProfilesFromDocuments(List<Document> documents) {
        List<Profile> profiles = new ArrayList<>();
        for (Document d : documents) {
            Optional<Profile> profile = profileRepository.findById(Long.valueOf(d.get(FieldNameLucene.PROFILE_ID)));
            profile.ifPresent(profiles::add);
        }
        return profiles;
    }

    public List<SearchResultItemGroup> GlobalSearch(Long profileId, String searchEntry) {
        List<SearchResultItemGroup> resultItems = new ArrayList<>();

        resultItems.add(getMessageResults(searchEntry, profileId));
        resultItems.add(getUserResults(searchEntry, profileId));
        resultItems.add(getChannelResults(searchEntry));

        return resultItems;
    }

    private SearchResultItemGroup getMessageResults(String searchEntry, Long profileId) {
        SearchResultItemGroup messagesItemGroup = SearchResultItemGroup.builder()
                .title("پیام ها")
                .items(new ArrayList<>())
                .build();
        if (searchEntry.strip().length() > 1) {
            for (Message m : searchInAllMessages(profileId, searchEntry)) {
                messagesItemGroup.getItems()
                        .add(SearchResultItem.builder()
                                .profile(m.getSender())
                                .text(m.getText())
                                .message_id(m.getMessageID())
                                .build());
            }
        }
        messagesItemGroup.setLength(messagesItemGroup.getItems().size());

        return messagesItemGroup;
    }

    private SearchResultItemGroup getChannelResults(String searchEntry) {
        SearchResultItemGroup channelsItemGroup = SearchResultItemGroup.builder()
                .title("کانال ها")
                .items(new ArrayList<>())
                .build();
        if (searchEntry.strip().length() > 2) {
            for (Profile p : searchInChannels(searchEntry)) {
                channelsItemGroup.getItems()
                        .add(SearchResultItem.builder()
                                .profile(p)
                                .text(p.getMemberCount() + " عضو ")
                                .message_id(0L)
                                .build());
            }
        }
        return channelsItemGroup;
    }

    private SearchResultItemGroup getUserResults(String searchEntry, Long profileId) {
        SearchResultItemGroup usersItemGroup = SearchResultItemGroup.builder()
                .title("کاربر ها")
                .items(new ArrayList<>())
                .build();
        if (searchEntry.strip().length() > 2) {
            for (Profile p : searchInUsers(searchEntry)) {
                //for savedMessage
                if (p.getProfileID().equals(profileId)) {
                    p.setProfileName("Saved Message");
                    p.setDefaultProfileColor("#2ee6ca");
                    p.setLastProfilePicture(null);
                }
                p.setStatus(//hasBlockedYou ? "Last seen a long time ago":
                        getLastSeen(p.getProfileID()));
                //check for people who blocked you
                Optional<Block> blockOptional = blockRepository.findById(new ProfilePareId(p, profileRepository.findById(profileId).get()));
                if (blockOptional.isPresent()) {
                    p.setLastProfilePicture(null);
                }
                p.setAccessPermission(getAccessPermission(profileRepository.findById(profileId).get(), p));

                //for block users

                usersItemGroup.getItems()
                        .add(SearchResultItem.builder()
                                .profile(p)
                                .text(p.getHandle())
                                .message_id(0L)
                                .build());
            }
        }
        return usersItemGroup;
    }

    public int getAccessPermission(Profile user, Profile chat) {
        //0 when you are not the admin and can not send a message in a channel
        //or blocked by each other
        ChatParticipant chatParticipant;
        try {
            chatParticipant = getParticipant(user, chat);
        } catch (Exception e) {
            return chat.getType() == ChatType.USER ? 1 : 0;
        }

        if (chat.getType() == ChatType.CHANNEL && !chatParticipant.isAdmin()) return 0;
        if (chat.getType() == ChatType.USER) {
            Optional<Block> blockPt1 = blockRepository.findById(new ProfilePareId(user, chat));
            Optional<Block> blockPt2 = blockRepository.findById(new ProfilePareId(chat, user));
            if (blockPt1.isPresent() || blockPt2.isPresent())
                return 0;
        }
        //if you are the admin of a chanel or group you've got permission to do whatever
        //can remove or edit his/her messages
        return chat.getType() != ChatType.USER && chatParticipant.isAdmin() ? 2 : 1;
    }

    public ChatParticipant getParticipant(Profile user, Profile dest) throws Exception {
        Optional<ChatParticipant> participant = chatParticipantRepository.findById(new ProfilePareId(user, dest));
        if (participant.isEmpty())
            throw new Exception("user is not a member of this chat");
        return participant.get();
    }

    public String getLastSeen(Long userId) {
        Optional<Account> accountOpt = accountRepository.findById(userId);
        if (accountOpt.isEmpty()) {
            Profile chat = profileRepository.findById(userId).get();
            return chat.getMemberCount() + " عضو";
        }
        Account account = accountOpt.get();
        if (userId.equals(2L))
            return "پیامرسان رسمی رسا";
        if (account.getProfile().isDeleted())
            return "آخرین حضور خیلی وقت پیش ";
        long daysPassed = ChronoUnit.DAYS.between(account.getLastSeen(), LocalDateTime.now());
        long hoursPassed = ChronoUnit.HOURS.between(account.getLastSeen(), LocalDateTime.now());
        long minutesPassed = ChronoUnit.MINUTES.between(account.getLastSeen(), LocalDateTime.now());
        if (account.isLastSeenSetting()) {
            if (daysPassed < 4)
                return "اخیرا دیده شده";
            else if (daysPassed <= 7)
                return "آخرین حضور در یک هفته گذشته";
            else if (daysPassed <= 31)
                return "آخرین حضور در یک ماه گذشته";
            return "آخرین حضور خیلی وقت پیش ";
        }

        if (minutesPassed <= 5)
            return "آنلاین";
        else if (minutesPassed <= 59)
            return "آخرین بازدید " + (minutesPassed - 5) + " دقیقه پیش ";
        else if (hoursPassed < 24)
            return "آخرین بازدید " + (hoursPassed) + " ساعت پیش ";
        else
            return "آخرین بازدید " + (daysPassed) + " روز پیش ";

    }

}
