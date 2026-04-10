package labex;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("labex.mapper")
public class LabexApplication {

    public static void main(String[] args) {
        SpringApplication.run(LabexApplication.class, args);
    }
}
