package se.omegapoint.megaarne.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.omegapoint.megaarne.model.entity.ReversedDatum;
import se.omegapoint.megaarne.repository.ReverseRepository;

import java.util.Objects;
import java.util.Optional;

@Service
public class ReverseService {



    private final ReverseRepository reverseRepository;

    @Autowired
    public ReverseService(ReverseRepository reverseRepository) {
        this.reverseRepository = reverseRepository;
    }

    public ReversedDatum reverse(final String data) {
        Objects.requireNonNull(data);
        try {
            return findReversedDataByData(data)
                    .orElseGet(() -> ReversedDatum.builder()
                            .withData(data)
                            .withReversedData(createAndPersistReversedDatum(data).reversedData)
                            .build());
        } catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private ReversedDatum createAndPersistReversedDatum(String data) {
        return reverseRepository.save(ReversedDatum.builder()
                .withData(data)
                .withReversedData(new StringBuilder(data).reverse().toString())
                .build());
    }

    private Optional<ReversedDatum> findReversedDataByData(final String data) {
        return Optional.ofNullable(reverseRepository.findByData(data));
    }
}
