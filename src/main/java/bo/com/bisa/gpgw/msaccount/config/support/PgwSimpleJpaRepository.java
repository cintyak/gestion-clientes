package bo.com.bisa.gpgw.msaccount.config.support;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.io.Serializable;
import java.util.List;

public class PgwSimpleJpaRepository<E, ID extends Serializable> extends SimpleJpaRepository<E, ID>
        implements PgwJpaRepository {

    private final EntityManager entityManager;

    public PgwSimpleJpaRepository(JpaEntityInformation<E, ID> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
    }

    @Override
    public <T> T findOne(QueryCallback<T> callback) {
        return callback.doWithEntityManager(this.entityManager);
    }

    @Override
    public <T> List<T> findAll(QueryCallback<List<T>> callback) {
        return callback.doWithEntityManager(this.entityManager);
    }

    @Override
    public <T> Page<T> findAll(Pageable pageable, QueryCallback<Page<T>> callback) {
        return callback.doWithEntityManager(this.entityManager);
    }

    protected static long executeCountQuery(TypedQuery<Long> query) {
        Assert.notNull(query, "TypedQuery must not be null!");

        List<Long> totals = query.getResultList();
        long total = 0L;

        for (Long element : totals) {
            total += element == null ? 0 : element;
        }

        return total;
    }
}
