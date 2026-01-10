package by.vstu.isit.documentprocessor;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.services.docx.FmeaWordGenerator;
import by.vstu.isit.documentprocessor.services.docx.PuWordGenerator;
import by.vstu.isit.documentprocessor.test.TestDockPackageFactory;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ConsoleMain implements CommandLineRunner {
    @Setter(onMethod_ = {@Autowired})
    private PuWordGenerator pGenerator;
    @Setter(onMethod_ = {@Autowired})
    private FmeaWordGenerator fGenerator;
    @Setter(onMethod_ = {@Autowired})
    private TestDockPackageFactory dockPackageFactory;

    @Override
    public void run(String... args) throws Exception {
        DockPackageDto dockPackageDto = dockPackageFactory.createTestDto();
        pGenerator.generate(dockPackageDto);
        fGenerator.generate(dockPackageDto);
    }

    static void main(String[] args) {
        SpringApplication.run(ConsoleMain.class, args);
    }
}
