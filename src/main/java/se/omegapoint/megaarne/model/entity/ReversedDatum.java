package se.omegapoint.megaarne.model.entity;

import javax.persistence.*;


@Entity
@Table(name = "reverse_datum")
public class ReversedDatum {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @Column(name = "data", unique=true)
    public String data;

    @Column(name = "reversed_data", unique=true)
    public String reversedData;

    public static ReverseDatumBuilder builder() {
        return new ReverseDatumBuilder();
    }

}
