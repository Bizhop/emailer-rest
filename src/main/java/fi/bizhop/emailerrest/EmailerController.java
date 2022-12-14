package fi.bizhop.emailerrest;

import fi.bizhop.emailerrest.model.Code;
import fi.bizhop.emailerrest.model.EmailWrapper;
import fi.bizhop.emailerrest.model.Report;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class EmailerController {
    final EmailerService service;

    @RequestMapping(value = "/codes", method = RequestMethod.GET, produces = "application/json")
    public List<Code> getCodes(@RequestParam(required = false) Boolean used) {
        if(used == null) {
            return service.getCodes();
        }
        else {
            return service.getCodes(used);
        }
    }

    @RequestMapping(value = "/codes/pg", method = RequestMethod.POST, consumes = "text/plain")
    public void importPgCodes(@RequestBody String body) {
        var codes = Utils.parsePgCodes(body);
        service.saveCodes(codes);
    }

    @RequestMapping(value = "/codes/nbdg", method = RequestMethod.POST, consumes = "text/plain")
    public void importNbdgCodes(@RequestBody String body, @RequestParam String valid) {
        var codes = Utils.parseNbdgCodes(body, valid);
        service.saveCodes(codes);
    }

    @RequestMapping(value = "/request", method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
    public @ResponseBody List<EmailWrapper> processRequest(@RequestBody String body, @RequestParam boolean send) {
        return service.processRequests(body, send);
    }

    @RequestMapping(value = "/report", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody Report report(@RequestParam String from, @RequestParam String to, HttpServletResponse response) {
        try {
            var fromTime = Utils.parseSimpleDate(from);
            var toTime = Utils.parseSimpleDate(to);
            return service.report(fromTime, toTime);
        } catch (DateTimeParseException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return Report.builder().error("Invalid date format").build();
        }
    }
}
