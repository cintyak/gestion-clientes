package bo.com.bisa.gpgw.msaccount.service;

import bo.com.bisa.gpgw.domain.UserBlocked;
import bo.com.bisa.gpgw.msaccount.repository.UserBlockedRepository;
import bo.com.bisa.gpgw.msaccount.service.dto.TransactionDTO;
import bo.com.bisa.gpgw.msaccount.service.dto.UserLockedDTO;
import bo.com.bisa.gpgw.msaccount.service.exceptions.NotFoundEntityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserBlockedService {
    @Autowired
    UserBlockedRepository userBlockRepository;

    public UserBlocked saveUserLocked(TransactionDTO transactionDTO, String reason   ) {
        log.info("--------------------< USER BLOCKED CREATE >");
        log.info("user blocked of transaction: {}", transactionDTO);
        final Boolean BLOCKED = true;
        UserBlocked userLocked = new UserBlocked();
        userLocked.setUserId(transactionDTO.getCustomerUserId());
        userLocked.setReason(reason);
        userLocked.setBlocked(BLOCKED);
        userLocked.setCompanyId(transactionDTO.getCompanyServiceId());
        log.info("USER BLOCKED: {}", userLocked);
        UserBlocked userLockedCreated = userBlockRepository.save(userLocked);
        log.info("user blocked created: {}", userLockedCreated);
        return userLockedCreated;
    }

    private UserLockedDTO toDto(UserBlocked userLocked) {
        UserLockedDTO userLockedDTO = new UserLockedDTO();
        userLockedDTO.setId(userLocked.getId());
        userLockedDTO.setUser(userLocked.getUserId());
        userLockedDTO.setLocked(userLocked.getBlocked());
        return userLockedDTO;
    }

    public UserBlocked unlockedUser(Long id) {
        final Boolean LOCKED = false;
        Optional<UserBlocked>  userLocked = userBlockRepository.findById(id);
        if(!userLocked.isPresent()) {
            throw new NotFoundEntityException("USER_BLOCKED", id);
        }
        userLocked.get().setBlocked(LOCKED);
        UserBlocked userLockedUpdated = userBlockRepository.save(userLocked.get());
        return userLockedUpdated;
    }

    public List<UserBlocked> getUsersLocked(){
        log.info("--------------------< GET USERS BLOCKED >");
        List<UserBlocked> list = userBlockRepository.findAll();
        log.info("List users Blocked {}", list);
        return list;
    }

    public UserBlocked getUsersLockedByUserId(String id) {
        final Boolean LOCKED = true;
        log.info("--------------------< GET BLOCKED USER >");
        UserBlocked userLocked = userBlockRepository.findByUserIdAndBlocked(id, LOCKED);
        log.info("User Blocked: {}", userLocked);
        return userLocked;
    }
}
