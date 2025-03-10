package org.shop.sportwebstore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shop.sportwebstore.model.ActivationType;
import org.shop.sportwebstore.model.entity.Activation;
import org.shop.sportwebstore.repository.ActivationRepository;
import org.shop.sportwebstore.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class SchedulerService {

    private final UserRepository userRepository;
    private final ActivationRepository activationRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void clearInactive() {
        List<Activation> codes = activationRepository.findAllByExpiresAtBefore(java.time.LocalDateTime.now());
        List<String> userIds = codes.stream()
                .filter(c -> c.getType() == ActivationType.REGISTRATION)
                .map(Activation::getUserId)
                .toList();
        activationRepository.deleteAll(codes);
        clearInactiveUsers(userIds);
        log.info("Deleted {} accounts.", userIds.size());
    }

    private void clearInactiveUsers(Collection<String> ids) {
        userRepository.deleteAllByIdIn(ids);
    }
}
