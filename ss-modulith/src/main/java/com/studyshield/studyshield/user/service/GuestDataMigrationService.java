package com.studyshield.studyshield.user.service;

import com.studyshield.studyshield.quiz.entity.QuizAttempt;
import com.studyshield.studyshield.quiz.repository.QuizAttemptRepository;
import com.studyshield.studyshield.quizresult.entity.QuizResult;
import com.studyshield.studyshield.quizresult.repository.QuizResultRepository;
import com.studyshield.studyshield.user.dto.auth.ClaimGuestDataResponse;
import com.studyshield.studyshield.user.entity.ChildProfile;
import com.studyshield.studyshield.user.entity.User;
import com.studyshield.studyshield.user.repository.ChildProfileRepository;
import com.studyshield.studyshield.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Moves data created under a guest account (keyed by device id)
 * to a registered user account. Safe to call even when the guest
 * account does not exist (returns zero counts).
 */
@Service
@Transactional
public class GuestDataMigrationService {

    private static final Logger log = LoggerFactory.getLogger(GuestDataMigrationService.class);

    private final UserRepository userRepository;
    private final QuizResultRepository quizResultRepository;
    private final ChildProfileRepository childProfileRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    public GuestDataMigrationService(
            UserRepository userRepository,
            QuizResultRepository quizResultRepository,
            ChildProfileRepository childProfileRepository,
            QuizAttemptRepository quizAttemptRepository) {
        this.userRepository = userRepository;
        this.quizResultRepository = quizResultRepository;
        this.childProfileRepository = childProfileRepository;
        this.quizAttemptRepository = quizAttemptRepository;
    }

    public ClaimGuestDataResponse migrate(String deviceId, Long targetUserId) {
        String guestEmail = "guest-" + deviceId + "@guest.local";

        if (!userRepository.existsByEmail(guestEmail)) {
            log.info("No guest account for deviceId={}; nothing to migrate", deviceId);
            return ClaimGuestDataResponse.success(0, 0, 0);
        }

        User guest = userRepository.findByEmail(guestEmail)
                .orElseThrow(() -> new IllegalArgumentException("Guest account not found: " + guestEmail));

        if (guest.getId().equals(targetUserId)) {
            log.info("Guest and target are the same account ({}); skipping migration", targetUserId);
            return ClaimGuestDataResponse.success(0, 0, 0);
        }

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Target account not found: " + targetUserId));

        // Signing up auto-creates a default "Kid 1 / Trial" child. During a
        // guest claim the guest's own children (moved below, or pushed from the
        // device) replace it, so drop the auto-created default to avoid a
        // duplicate kid card on the new account.
        removeSignupDefaultKid(targetUserId);

        int resultsMoved = moveQuizResults(guest.getId(), targetUserId);
        int attemptsMoved = moveAttempts(guest.getId(), targetUserId);
        int childrenMoved = moveChildren(guest, target);

        log.info("Migrated guest data from deviceId={}: {} results, {} children, {} attempts",
                deviceId, resultsMoved, childrenMoved, attemptsMoved);

        return ClaimGuestDataResponse.success(resultsMoved, childrenMoved, attemptsMoved);
    }

    private int moveQuizResults(Long guestUserId, Long targetUserId) {
        List<QuizResult> results = quizResultRepository.findByAccountIdOrderByCompletedAtDesc(guestUserId);
        int count = 0;
        for (QuizResult r : results) {
            r.setAccountId(targetUserId);
            quizResultRepository.save(r);
            count++;
        }
        return count;
    }

    private int moveAttempts(Long guestUserId, Long targetUserId) {
        List<QuizAttempt> attempts = quizAttemptRepository.findByUserId(guestUserId);
        int count = 0;
        for (QuizAttempt a : attempts) {
            a.setUserId(targetUserId);
            quizAttemptRepository.save(a);
            count++;
        }
        return count;
    }

    private int moveChildren(User guest, User target) {
        List<ChildProfile> children = childProfileRepository.findByUserId(guest.getId());
        int count = 0;
        for (ChildProfile c : children) {
            c.setUser(target);
            childProfileRepository.save(c);
            count++;
        }
        return count;
    }

    /**
     * Deletes the signup auto-created default child ("Kid 1"/"Trial", age 0,
     * never populated) from the target account. Guarded by name+class+age so a
     * real child's profile is never touched.
     */
    private void removeSignupDefaultKid(Long targetUserId) {
        List<ChildProfile> children = childProfileRepository.findByUserId(targetUserId);
        for (ChildProfile c : children) {
            if (ChildProfileService.DEFAULT_KID_NAME.equals(c.getName())
                    && ChildProfileService.DEFAULT_KID_CLASS.equals(c.getStudentClass())
                    && c.getAge() == 0) {
                log.info("Removing signup default child id={} from target account {}", c.getId(), targetUserId);
                childProfileRepository.delete(c);
            }
        }
    }
}
