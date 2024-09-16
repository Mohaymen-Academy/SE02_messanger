package com.mohaymen.service;

import com.mohaymen.model.entity.Account;
import com.mohaymen.model.entity.Profile;
import com.mohaymen.repository.*;
import com.mohaymen.security.PasswordHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    private final ProfileRepository profileRepository;

    private final ProfilePictureRepository profilePictureRepository;

    private final SearchService searchService;

    public AccountService(AccountRepository accountRepository, ProfileRepository profileRepository, ProfilePictureRepository profilePictureRepository, SearchService searchService) {
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
        this.profilePictureRepository = profilePictureRepository;
        this.searchService = searchService;
    }

    public void UpdateLastSeen(Long userId) throws Exception {
        Account account = getAccount(userId);
        if (account.getProfile().isDeleted())
            return;
        account.setLastSeen(LocalDateTime.now());
        accountRepository.save(account);
    }

    private Account getAccount(Long userId) throws Exception {
        Optional<Account> optionalAccount = accountRepository.findById(userId);
        if (optionalAccount.isEmpty()) throw new Exception("acount not found");
        return optionalAccount.get();
    }

    /**
     * Last seen recently — covers anything between 1 second and 2-3 days.
     * Last seen within a week — between 2-3 and seven days.
     * Last seen within a month — between 6-7 days and a month.
     * Last seen a long time ago — more than a month (this is also always shown to blocked users)
     */
    public String getLastSeen(Long userId) {
        Account account;
        try {
            account = getAccount(userId);
        } catch (Exception e) {
            Profile chat=profileRepository.findById(userId).get();
            return chat.getMemberCount()+" عضو";
        }
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
    public void deleteProfile(Profile profile) {
        UUID uuid = UUID.randomUUID();
        profilePictureRepository.deleteByProfile(profile);
        profile.setHandle(profile.getHandle() + uuid);
        profile.setProfileName("DELETED");
        profile.setDeleted(true);
        profile.setLastProfilePicture(null);
        profileRepository.save(profile);
    }

    @Transactional
    public void deleteAccount(Long id, byte[] password) throws Exception {
        Profile profile = profileRepository.findById(id).get();
        Account account = accountRepository.findByProfile(profile).get();

        byte[] checkPassword = PasswordHandler.getHashed(
                PasswordHandler.combineArray(password, account.getSalt()));

        if (!Arrays.equals(checkPassword, account.getPassword()))
            throw new Exception("Wrong password");

        deleteProfile(profile);
        searchService.deleteUser(profile);
        accountRepository.delete(account);
    }

}
