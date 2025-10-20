package com.ognjen.budgetok.infrastructure.persistence;

import com.ognjen.budgetok.application.Envelope;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JdbcEnvelopeRepository extends ListCrudRepository<Envelope, Long> {

}
