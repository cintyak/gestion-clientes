package bo.com.bisa.gpgw.msaccount.repository;

import bo.com.bisa.gpgw.domain.Authority;
import bo.com.bisa.gpgw.domain.PaymentGateway;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthorityRepository extends JpaRepository<Authority, String> {
        List<Authority> findByName(String name);
}
