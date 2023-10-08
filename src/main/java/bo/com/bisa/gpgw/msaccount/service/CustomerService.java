package bo.com.bisa.gpgw.msaccount.service;

import bo.com.bisa.gpgw.domain.Customer;
import bo.com.bisa.gpgw.domain.PgwMid;
import bo.com.bisa.gpgw.msaccount.repository.CustomerRepository;
import bo.com.bisa.gpgw.msaccount.service.dto.CustomerDTO;
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
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerDTO save(CustomerDTO customerDTO) {
	customerDTO.setState("CREADO");
        Customer customer = this.toEntity(customerDTO);
        customer = customerRepository.save(customer);
        log.info("Request to save Customer : {}", customerDTO);
        return this.toDto(customer);
    }

    @Transactional
    public Optional<CustomerDTO> partialUpdate(CustomerDTO customerDTO) {
        return customerRepository
            .findById(customerDTO.getId())
            .map(existingCustomer -> {

                if (customerDTO.getEmail() != null) {
                    existingCustomer.setEmail(customerDTO.getEmail());
                }
                if (customerDTO.getPhone() != null) {
                    existingCustomer.setPhone(customerDTO.getPhone());
                }
                if (customerDTO.getOcupation() != null) {
                    existingCustomer.setOcupation(customerDTO.getOcupation());
                }
                if (customerDTO.getAge() != null) {
                    existingCustomer.setAge(customerDTO.getAge());
                }
             
                if (customerDTO.getState() != null) {
                    existingCustomer.setState(customerDTO.getState());
                }
                log.info("Request to partially update Customer : {}", existingCustomer);
                return existingCustomer;
            })
            .map(this::toDto);
    }

    public Page<CustomerDTO> findAll(Pageable pageable) {
        return customerRepository.findAll(pageable).map(this::toDto);
    }

    public Optional<CustomerDTO> findOne(Long id) {
        return customerRepository.findById(id).map(this::toDto);
    }

    @Transactional
    public void delete(Long id) {
        customerRepository.deleteById(id);
        log.debug("Request to delete Customer : {}", id);
    }

    public Customer toEntity(CustomerDTO dto) {
        if (dto == null) {
            return null;
        }

        Customer customer = new Customer();

        customer.setId(dto.getId());


        return customer;
    }

    public CustomerDTO toDto(Customer s) {
        if (s == null) {
            return null;
        }

        CustomerDTO customerDTO = new CustomerDTO();

        customerDTO.setId(s.getId());

        return customerDTO;
    }
}
