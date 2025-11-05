package com.ognjen.template.monolith.repositories;

import com.ognjen.template.monolith.models.Envelope;
import org.springframework.stereotype.Repository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryEnvelopeRepository implements EnvelopeRepository {
    private final Map<Long, Envelope> envelopes = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Envelope save(Envelope envelope) {
        if (envelope.getId() == 0) {
            envelope.setId(idGenerator.getAndIncrement());
        }
        envelopes.put(envelope.getId(), envelope);
        return envelope;
    }

    @Override
    public Optional<Envelope> findById(long id) {
        return Optional.ofNullable(envelopes.get(id));
    }

    @Override
    public List<Envelope> findAll() {
        return new java.util.ArrayList<>(envelopes.values());
    }

    @Override
    public void delete(long id) {
        envelopes.remove(id);
    }

    @Override
    public void update(Envelope envelope) {
        if (envelopes.containsKey(envelope.getId())) {
            envelopes.put(envelope.getId(), envelope);
        }
    }
}
