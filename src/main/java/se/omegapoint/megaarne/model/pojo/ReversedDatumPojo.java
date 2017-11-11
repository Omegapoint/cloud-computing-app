package se.omegapoint.megaarne.model.pojo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReversedDatumPojo {

    public String applicationName;
    public String timeStamp;
    public String data;

    public ReversedDatumPojo() {
        this.applicationName = "Reversed-Richard";
    }

    public ReversedDatumPojo(String s) {
        this();
        this.data = s;
        this.timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static ReversedDatumPojoBuilder builder() {
        return new ReversedDatumPojoBuilder();
    }
}
