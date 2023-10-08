package bo.com.bisa.gpgw.msaccount.repository;

import bo.com.bisa.gpgw.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByExternalUserId(String customerUserId);
    Optional<Customer> findByExternalUserIdAndMidId(String customerUserId);
}
