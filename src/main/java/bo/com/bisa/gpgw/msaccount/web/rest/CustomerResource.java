package bo.com.bisa.gpgw.msaccount.web.rest;

import bo.com.bisa.gpgw.domain.PgwMid;
import bo.com.bisa.gpgw.msaccount.repository.CustomerRepository;
import bo.com.bisa.gpgw.msaccount.service.CustomerService;
import bo.com.bisa.gpgw.msaccount.service.dto.CustomerDTO;
import bo.com.bisa.gpgw.msaccount.web.rest.errors.NotFoundAlertException;
import bo.com.bisa.gpgw.msaccount.web.rest.util.PaginationUtil;
import bo.com.bisa.gpgw.msaccount.web.rest.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class CustomerResource {

    private final CustomerService customerService;

    private final CustomerRepository customerRepository;
    

    @PostMapping("/customers")
    public ResponseEntity<CustomerDTO> createCustomer(@Valid @RequestBody CustomerDTO customerDTO) throws URISyntaxException {
        log.debug("REST request to save Customer : {}", customerDTO);
  if (customerDTO.getAge()<20) throw new NotFoundAlertException();
        CustomerDTO result = customerService.save(customerDTO);
        return ResponseEntity
            .created(new URI("/api/customers/" + result.getId()))
            .body(result);
    }

    @PutMapping("/customers")
    public ResponseEntity<CustomerDTO> updateCustomer(@Valid @RequestBody CustomerDTO customerDTO) {
        log.debug("REST request to update Customer : {}", customerDTO);
        return ResponseUtil.wrapOrNotFound(customerService.partialUpdate(customerDTO));
    }

    @GetMapping("/customers")
    public ResponseEntity<List<CustomerDTO>> getAllCustomers(Pageable pageable) {
        log.debug("REST request to get a page of Customers");
        Page<CustomerDTO> page = customerService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.addTotalCountHttpHeaders(page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<CustomerDTO> getCustomer(@PathVariable Long id) {
        log.debug("REST request to get Customer : {}", id);
        Optional<CustomerDTO> customerDTO = customerService.findOne(id);
        return ResponseUtil.wrapOrNotFound(customerDTO);
    }

    @DeleteMapping("/customers/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        log.debug("REST request to delete Customer : {}", id);
        if (!customerRepository.existsById(id)) throw new NotFoundAlertException();
        customerService.delete(id);
        return ResponseEntity
            .noContent()
            .build();
    }
}
