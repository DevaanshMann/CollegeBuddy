package com.collegebuddy.email;

import org.springframework.stereotype.Service;

/*
    Outbound email adapter. In tests you can mock/WireMock this.
 */

@Service
public class EmailService {

    public void sendVerificationEmail(String toAddress, String tokenLink){
//        TODO: send email
    }

    public void sendNotificationEmail(String toAddress, String body){
//        TODO: optional general-purpose email
    }
}
