package kspt.bank.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kspt.bank.domain.entities.ManipulationLog;
import kspt.bank.enums.CellApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@ConditionalOnWebApplication
@RestController
@AllArgsConstructor
public class WebServer {
    @Autowired
    private final BankVaultFacade bankVaultFacade;

    @Autowired
    private final ManipulationLog manipulationLog;

    @GetMapping("/")
    void hello(HttpServletResponse httpResponse)
    throws IOException {
        httpResponse.sendRedirect("index.html");
    }

    @GetMapping("/apps")
    String applications()
    throws JsonProcessingException {
        final List<ApplicationInfo> data = bankVaultFacade.findAllCellApplications().stream()
                .map(app -> new ApplicationInfo(app.getId(), app.getLeaseholder().firstName,
                        app.getCell().getCodeName(), app.getLeasePeriod().getDays(), app.getStatus()))
                .collect(Collectors.toList());
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(data);
    }

    @GetMapping("/log")
    String log()
    throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(manipulationLog.getManipulations());
    }

    @Value
    private static class ApplicationInfo {
        Integer id;

        String leaseholderName;

        String cellName;

        Integer leasePeriodInDays;

        CellApplicationStatus status;
    }
}
