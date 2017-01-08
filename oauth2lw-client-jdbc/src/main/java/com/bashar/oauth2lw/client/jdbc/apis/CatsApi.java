package com.bashar.oauth2lw.client.jdbc.apis;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Bashar on 2017-01-08.
 */
@RestController
@RequestMapping("/protected/cats")
public class CatsApi {

    @RequestMapping("/")
    public Map<String, String> index() {
        HashMap<String, String> msg = new HashMap<>();
        msg.put("say", "mewo");
        return msg;
    }
}
