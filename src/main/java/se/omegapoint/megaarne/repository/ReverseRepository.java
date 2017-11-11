package se.omegapoint.megaarne.repository;

import org.springframework.data.repository.CrudRepository;
import se.omegapoint.megaarne.model.entity.ReversedDatum;

public interface ReverseRepository extends CrudRepository<ReversedDatum, Long> {
    ReversedDatum findByData(String data);
}
