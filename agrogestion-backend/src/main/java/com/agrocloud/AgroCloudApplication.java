package com.agrocloud;

import com.agrocloud.config.CargadorVariablesEntornoLocal;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AgroCloudApplication {

    public static void main(String[] args) {
        CargadorVariablesEntornoLocal.cargar();
        SpringApplication.run(AgroCloudApplication.class, args);
    }

}
