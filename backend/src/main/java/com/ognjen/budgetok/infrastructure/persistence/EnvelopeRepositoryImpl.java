package com.ognjen.budgetok.infrastructure.persistence;

import com.ognjen.budgetok.application.Envelope;
import com.ognjen.budgetok.application.EnvelopeNotFoundException;
import com.ognjen.budgetok.application.EnvelopeRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
@RequiredArgsConstructor
public class EnvelopeRepositoryImpl implements EnvelopeRepository {

  private final JdbcEnvelopeRepository jdbcRepository;

  @Override
  public List<Envelope> findAll() {
    return jdbcRepository.findAll();
  }

  @Override
  public Envelope findById(Long id) {
    return jdbcRepository.findById(id).orElse(null);
  }

  @Override
  public Envelope save(Envelope envelope) {
    return jdbcRepository.save(envelope);
  }

  @Override
  public void deleteById(Long id) {
    jdbcRepository.deleteById(id);
  }
}
