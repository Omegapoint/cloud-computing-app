package se.omegapoint.megaarne.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import se.omegapoint.megaarne.model.pojo.ReversedDatumPojo;
import se.omegapoint.megaarne.service.ReverseService;

@RestController
@RequestMapping("${spring.application.name}")
public class ReverseController {

    @Value("${spring.application.name}")
    private String appName;

    private final ReverseService reverseService;

    @Autowired
    public ReverseController(ReverseService reverseService) {
        this.reverseService = reverseService;
    }

    @RequestMapping(value = "/ping", method = RequestMethod.GET)
    public String ping() {
        return "pong";
    }

    @RequestMapping(value = "/reverse/{data}", method = RequestMethod.GET, produces = "application/json")
    public ReversedDatumPojo reverse(@PathVariable("data") String data) {
        try {
            return ReversedDatumPojo.builder()
                    .withTimeStamp()
                    .withAppName(this.appName)
                    .withReversedData(reverseService.reverse(data).reversedData)
                    .build();
        } catch (Exception e) {
            return null; //TODO implement exception handling
        }
    }

}
