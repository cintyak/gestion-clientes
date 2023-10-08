package bo.com.bisa.gpgw.msaccount.service;

import bo.com.bisa.gpgw.domain.Person;
import bo.com.bisa.gpgw.msaccount.repository.PersonRepository;
import bo.com.bisa.gpgw.msaccount.service.dto.PersonDTO;
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
public class PersonService {

    private final PersonRepository PersonRepository;

    @Transactional
    public PersonDTO save(PersonDTO PersonDTO) {
        Person Person = this.toEntity(PersonDTO);
        Person = PersonRepository.save(Person);
        log.info("Request to save Person : {}", PersonDTO);
        return this.toDto(Person);
    }

    @Transactional
    public Optional<PersonDTO> partialUpdate(PersonDTO PersonDTO) {
        return PersonRepository
            .findById(PersonDTO.getId())
            .map(existingPerson -> {

                if (PersonDTO.getPersonUserId() != null) {
                    existingPerson.setPersonUserId(PersonDTO.getPersonUserId());
                }
                if (PersonDTO.getFirstName() != null) {
                    existingPerson.setFirstName(PersonDTO.getFirstName());
                }
                if (PersonDTO.getLastName() != null) {
                    existingPerson.setLastName(PersonDTO.getLastName());
                }
                if (PersonDTO.getAddress() != null) {
                    existingPerson.setAddress(PersonDTO.getAddress());
                }
             
                if (PersonDTO.getBithdate() != null) {
                    existingPerson.setBithdate(PersonDTO.getBithdate());
                }
                if (PersonDTO.getDocumentId() != null) {
                    existingPerson.setDocumentId(PersonDTO.getDocumentId());
                }
                log.info("Request to partially update Person : {}", existingPerson);
                return existingPerson;
            })
            .map(this::toDto);
    }

    public Page<PersonDTO> findAll(Pageable pageable) {
        return PersonRepository.findAll(pageable).map(this::toDto);
    }

    public Optional<PersonDTO> findOne(Long id) {
        return PersonRepository.findById(id).map(this::toDto);
    }

    @Transactional
    public void delete(Long id) {
        PersonRepository.deleteById(id);
        log.debug("Request to delete Person : {}", id);
    }

    public Person toEntity(PersonDTO dto) {
        if (dto == null) {
            return null;
        }

        Person Person = new Person();

        Person.setId(dto.getId());


        return Person;
    }

    public PersonDTO toDto(Person s) {
        if (s == null) {
            return null;
        }

        PersonDTO PersonDTO = new PersonDTO();

        PersonDTO.setId(s.getId());

        return PersonDTO;
    }
}
