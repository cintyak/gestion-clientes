package bo.com.bisa.gpgw.msaccount.service;

import bo.com.bisa.gpgw.domain.Company;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Transactional
    public Company save(Company company) {
        company = companyRepository.save(company);
        log.info("Request to save Company : {}", company);
        return company;
    }

    @Transactional
    public Optional<Company> partialUpdate(Company company) {
        return companyRepository
            .findById(company.getId())
            .map(existingCompany -> {
                if (company.getName() != null) {
                    existingCompany.setName(company.getName());
                }
                if (company.getBusinessName() != null) {
                    existingCompany.setBusinessName(company.getBusinessName());
                }
                if (company.getCity() != null) {
                    existingCompany.setCity(company.getCity());
                }
                if (company.getPhone() != null) {
                    existingCompany.setPhone(company.getPhone());
                }
                if (company.getAddress() != null) {
                    existingCompany.setAddress(company.getAddress());
                }
                if (company.getNotificationEmail() != null) {
                    existingCompany.setNotificationEmail(company.getNotificationEmail());
                }
                if (company.getActive() != null) {
                    existingCompany.setActive(company.getActive());
                }
                log.info("Request to partially update Company : {}", company);
                return existingCompany;
            });
    }

    public Page<Company> findAll(Pageable pageable) {
        return companyRepository.findAll(pageable);
    }

    public Optional<Company> findOne(Long id) {
        return companyRepository.findById(id);
    }

    @Transactional
    public void delete(Long id) {
        companyRepository.deleteById(id);
        log.info("Request to delete Company : {}", id);
    }
}
