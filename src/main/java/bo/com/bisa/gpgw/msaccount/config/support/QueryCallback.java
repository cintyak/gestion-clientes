package bo.com.bisa.gpgw.msaccount.config.support;

import javax.persistence.EntityManager;

@FunctionalInterface
public interface QueryCallback<T> {

    T doWithEntityManager(EntityManager entityManager);
}
