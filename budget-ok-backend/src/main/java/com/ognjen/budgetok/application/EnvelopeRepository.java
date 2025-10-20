package com.ognjen.budgetok.application;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface EnvelopeRepository {
//public interface EnvelopeRepository extends ListCrudRepository<Envelope, Long> {

//    @Query("SELECT * FROM envelopes WHERE name = :name")
//    Optional<Envelope> findByName(@Param("name") String name);
    
    List<Envelope> findAll();
    
    Envelope findById(Long id);
    
    Envelope save(Envelope envelope);
    
    void deleteById(Long id);
}
