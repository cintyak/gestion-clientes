package bo.com.bisa.gpgw.msaccount.web.rest;

import bo.com.bisa.gpgw.domain.UserBlocked;
import bo.com.bisa.gpgw.msaccount.service.UserBlockedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users-locked")
public class UserLockedResource {
    @Autowired
    UserBlockedService userBlockedService;

    @GetMapping
    public ResponseEntity<List<UserBlocked>> getUsersLocked(){
        return ResponseEntity.ok(userBlockedService.getUsersLocked());
    }

    @PutMapping("unlock/{id}")
    public ResponseEntity<UserBlocked> unlockedUser(@PathVariable("id") Long id){
        return ResponseEntity.ok(userBlockedService.unlockedUser(id));
    }
}

