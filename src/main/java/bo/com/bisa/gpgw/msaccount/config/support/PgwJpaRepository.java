package bo.com.bisa.gpgw.msaccount.config.support;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface PgwJpaRepository {

    <T> T findOne(QueryCallback<T> callback);

    <T> List<T> findAll(QueryCallback<List<T>> callback);

    <T> Page<T> findAll(Pageable pageable, QueryCallback<Page<T>> callback);
}
