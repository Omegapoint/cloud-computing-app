package se.omegapoint.megaarne.model.pojo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReversedDatumPojoBuilder {

    private String reversedData;
    private String applicationName;
    private LocalDateTime timeStamp;

    public ReversedDatumPojoBuilder withReversedData(String reversedData) {
        this.reversedData = reversedData;
        return this;
    }

    public ReversedDatumPojo build() {
        ReversedDatumPojo pojo = new ReversedDatumPojo();
        pojo.applicationName = this.applicationName;
        pojo.data = this.reversedData;
        pojo.timeStamp = timeStamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return pojo;
    }

    public ReversedDatumPojoBuilder withTimeStamp() {
        this.timeStamp = LocalDateTime.now();
        return this;
    }

    public ReversedDatumPojoBuilder withAppName(String appName) {
        this.applicationName = appName;
        return this;
    }
}
