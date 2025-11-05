package com.ognjen.template.monolith.repositories;

import com.ognjen.template.monolith.models.Envelope;
import java.util.List;
import java.util.Optional;

public interface EnvelopeRepository {
    Envelope save(Envelope envelope);
    Optional<Envelope> findById(long id);
    List<Envelope> findAll();
    void delete(long id);
    void update(Envelope envelope);
}
