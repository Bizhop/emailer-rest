package fi.bizhop.emailerrest;

import fi.bizhop.emailerrest.model.Code;
import fi.bizhop.emailerrest.model.EmailWrapper;
import fi.bizhop.emailerrest.model.Report;
import fi.bizhop.emailerrest.model.SheetsRequest;
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

    @RequestMapping(value = "/codes", method = RequestMethod.POST, consumes = "text/plain")
    public List<Code> importCodes(@RequestBody String body, @RequestParam String valid, @RequestParam String store) {
        if("PG".equals(store)) {
            var codes = Utils.parsePgCodes(body, valid);
            return service.saveCodes(codes);
        }
        else if("NBDG".equals(store)) {
            var codes = Utils.parseNbdgCodes(body, valid);
            return service.saveCodes(codes);
        }
        else {
            throw new UnsupportedOperationException("Incompatible store");
        }
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

    @RequestMapping(value = "/sheetsrequests/new", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody List<SheetsRequest> getNewSheetsRequests() {
        return service.getNewSheetsRequests();
    }
}
