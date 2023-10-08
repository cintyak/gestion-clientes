package bo.com.bisa.gpgw.msaccount.web.rest;

import bo.com.bisa.gpgw.domain.Company;
import bo.com.bisa.gpgw.domain.PaymentGateway;
import bo.com.bisa.gpgw.domain.Transaction;
import bo.com.bisa.gpgw.msaccount.repository.PersonRepository;
import bo.com.bisa.gpgw.msaccount.service.PersonService;
import bo.com.bisa.gpgw.msaccount.service.dto.PersonDTO;
import bo.com.bisa.gpgw.msaccount.web.rest.errors.NotFoundAlertException;
import bo.com.bisa.gpgw.msaccount.web.rest.response.*;
import bo.com.bisa.gpgw.msaccount.web.rest.util.PaginationUtil;
import bo.com.bisa.gpgw.msaccount.web.rest.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class PersonResource {

    
    private final PersonService PersonService;

    private final PersonRepository PersonRepository;
    

    @PostMapping("/person")
    public ResponseEntity<PersonDTO> createPerson(@Valid @RequestBody PersonDTO PersonDTO) throws URISyntaxException {
        log.debug("REST request to save Person : {}", PersonDTO);
        PersonDTO result = PersonService.save(PersonDTO);
        return ResponseEntity
            .created(new URI("/api/person/" + result.getId()))
            .body(result);
    }

    @PutMapping("/person")
    public ResponseEntity<PersonDTO> updatePerson(@Valid @RequestBody PersonDTO PersonDTO) {
        log.debug("REST request to update Person : {}", PersonDTO);
        return ResponseUtil.wrapOrNotFound(PersonService.partialUpdate(PersonDTO));
    }

    @GetMapping("/person")
    public ResponseEntity<List<PersonDTO>> getAllPersons(Pageable pageable) {
        log.debug("REST request to get a page of Persons");
        Page<PersonDTO> page = PersonService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.addTotalCountHttpHeaders(page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/person/{id}")
    public ResponseEntity<PersonDTO> getPerson(@PathVariable Long id) {
        log.debug("REST request to get Person : {}", id);
        Optional<PersonDTO> PersonDTO = PersonService.findOne(id);
        return ResponseUtil.wrapOrNotFound(PersonDTO);
    }

    @DeleteMapping("/person/{id}")
    public ResponseEntity<Void> deletePerson(@PathVariable Long id) {
        log.debug("REST request to delete Person : {}", id);
        if (!PersonRepository.existsById(id)) throw new NotFoundAlertException();
        PersonService.delete(id);
        return ResponseEntity
            .noContent()
            .build();
    }
}
