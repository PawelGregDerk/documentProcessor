package by.vstu.isit.documentprocessor;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.services.docx.abstracts.AbstractWordGenerator;
import by.vstu.isit.documentprocessor.test.TestDockPackageFactory;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class ConsoleMain implements CommandLineRunner {
    @Setter(onMethod_ = {@Autowired})
    List<AbstractWordGenerator> generators;
    @Setter(onMethod_ = {@Autowired})
    private TestDockPackageFactory dockPackageFactory;

    @Override
    public void run(String... args) throws Exception {
        DockPackageDto dto = dockPackageFactory.createTestDto();
       // System.out.println(dto);
        for (var generator : generators) {
            generator.generate(dto);
        }
    }

    static void main(String[] args) {
        SpringApplication.run(ConsoleMain.class, args);
    }
}
