package org.uestc.weglas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication
public class LabEquipmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(LabEquipmentApplication.class, args);
    }
}
