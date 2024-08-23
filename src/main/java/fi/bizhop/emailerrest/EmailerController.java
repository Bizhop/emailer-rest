package fi.bizhop.emailerrest;

import fi.bizhop.emailerrest.model.Code;
import fi.bizhop.emailerrest.model.EmailWrapper;
import fi.bizhop.emailerrest.model.Report;
import fi.bizhop.emailerrest.model.SheetsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.format.DateTimeParseException;
import java.util.List;

import static javax.servlet.http.HttpServletResponse.SC_OK;

@RestController
@RequiredArgsConstructor
public class EmailerController {
    final EmailerService service;

    @RequestMapping(value = "/codes", method = RequestMethod.GET, produces = "application/json")
    public List<Code> getCodes(@RequestParam(required = false) Boolean used, HttpServletResponse response) {
        response.setStatus(SC_OK);
        if(used == null) {
            return service.getCodes();
        }
        else {
            return service.getCodes(used);
        }
    }

    @RequestMapping(value = "/codes", method = RequestMethod.POST, consumes = "text/plain")
    public List<Code> importCodes(@RequestBody String body, @RequestParam String valid, @RequestParam String store, HttpServletResponse response) {
        response.setStatus(SC_OK);
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

    @RequestMapping(value = "/report", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody Report report(@RequestParam String from, @RequestParam String to, HttpServletResponse response) {
        response.setStatus(SC_OK);
        try {
            var fromTime = Utils.parseSimpleDate(from);
            var toTime = Utils.parseSimpleDate(to);
            return service.report(fromTime, toTime);
        } catch (DateTimeParseException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return Report.builder().error("Invalid date format").build();
        }
    }

    @RequestMapping(value = "/test-email", method = RequestMethod.POST)
    public void testEmail(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(SC_OK);
        var auth = request.getHeader("Authorization");
        service.sendEmailWithJWT(auth);
    }

    @RequestMapping(value = "/sheetsrequests/new", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody List<SheetsRequest> getNewSheetsRequests(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(SC_OK);
        var auth = request.getHeader("Authorization");
        return service.getNewSheetsRequests(auth);
    }

    @RequestMapping(value = "/sheetsrequests", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody List<SheetsRequest> getSheetRequests(@RequestParam(required = false) SheetsRequest.Status status, HttpServletResponse response) {
        response.setStatus(SC_OK);
        return service.getSheetRequests(status);
    }

    @RequestMapping(value = "/sheetsrequests/complete", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public List<EmailWrapper> completeSheetsRequests(@RequestBody List<Long> ids, @RequestParam boolean send, @RequestParam(defaultValue = "Kivikon viikkokisat") String competition, @RequestParam(required = false) String date, HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(SC_OK);
        var auth = request.getHeader("Authorization");
        return service.completeSheetsRequests(ids, send, competition, date, auth);
    }

    @RequestMapping(value = "/sheetsrequests/reject", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public List<SheetsRequest> rejectSheetsRequests(@RequestBody List<Long> ids, HttpServletResponse response) {
        response.setStatus(SC_OK);
        return service.rejectSheetsRequests(ids);
    }
}
