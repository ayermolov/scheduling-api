package com.dispatch.schedulingapi;

import com.dispatch.schedulingapi.service.TwilioSmsService;
import com.dispatch.schedulingapi.service.ZohoInvoiceService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SchedulingApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchedulingApiApplication.class, args);

    }


}