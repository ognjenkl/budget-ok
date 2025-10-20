package com.ognjen.budgetok.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryEnvelopeRepository implements EnvelopeRepository {

    private final List<Envelope> envelopes = new ArrayList<>();
    private long nextId = 1;

  @Override
  public List<Envelope> findAll() {
    return List.of();
  }

  @Override
  public Envelope findById(Long id) {
    return null;
  }

  @Override
    public Envelope save(Envelope envelope) {
        if (envelope.getId() == null) {
            envelope.setId(nextId++);
            envelopes.add(envelope);
        } else {
            deleteById(envelope.getId());
            envelopes.add(envelope);
        }
        return envelope;
    }

  @Override
  public void deleteById(Long id) {

  }
}
