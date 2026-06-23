package com.rc.readcompass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ReadCompassApplication {

  public static void main(String[] args) {
    SpringApplication.run(ReadCompassApplication.class, args);
  }

}
