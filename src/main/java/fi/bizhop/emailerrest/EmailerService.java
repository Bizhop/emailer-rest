package fi.bizhop.emailerrest;

import fi.bizhop.emailerrest.db.CodeRepository;
import fi.bizhop.emailerrest.db.SentRepository;
import fi.bizhop.emailerrest.db.SheetsRequestRepository;
import fi.bizhop.emailerrest.model.Code;
import fi.bizhop.emailerrest.model.Email;
import fi.bizhop.emailerrest.model.EmailWrapper;
import fi.bizhop.emailerrest.model.Report;
import fi.bizhop.emailerrest.model.ReportEmail;
import fi.bizhop.emailerrest.model.Request;
import fi.bizhop.emailerrest.model.Sent;
import fi.bizhop.emailerrest.model.SheetsRequest;
import fi.bizhop.emailerrest.model.Store;
import jdk.jshell.execution.Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static fi.bizhop.emailerrest.model.SheetsRequest.Status.*;
import static fi.bizhop.emailerrest.model.Store.NBDG;
import static fi.bizhop.emailerrest.model.Store.PG;

@Service
@RequiredArgsConstructor
public class EmailerService {
    final CodeRepository codeRepository;
    final SentRepository sentRepository;
    final SheetsRequestRepository sheetsRequestRepository;
    final GmailAPI gmailAPI;
    final SheetsAPI sheetsAPI;

    private final String EMAIL_FROM = "rahastonhoitaja@discgolfvikings.fi";
    private final String EMAIL_SUBJECT = "Lahjakortti, <COMPETITION>";
    private final String EMAIL_TEMPLATE = "Hei,\n\nja onneksi olkoon! Voitit Disc Golf Vikingsin järjestämässä kilpailussa (<COMPETITION>, <DATE>) lahjakortin arvoltaan 15€.\n\nOhessa koodi, jonka voit käyttää <STORE>-kauppaan. Koodi on voimassa <VALID> asti.\n\n<CODE> 15.00 €\n\nYstävällisin terveisin:\n\nVille Piispa\n\nDisc Golf Vikings ry:n rahastonhoitaja\nDisc Golf Vikings Viikkokisatiimi\n";

    private final Comparator<SheetsRequest> compareRequests = (a, b) -> {
        try {
            var dateA = Utils.parseCompetitionDate(a.getCompetitionDate());
            var dateB = Utils.parseCompetitionDate(b.getCompetitionDate());
            return dateA.compareTo(dateB);
        } catch (Exception e) {
            System.out.printf("Error comparing dates: %s - %s\n", a.getCompetitionDate(), b.getCompetitionDate());
            return 0;
        }
    };

    public List<Code> getCodes() { return codeRepository.findAll();  }

    public List<Code> getCodes(Boolean used) { return codeRepository.findAllByUsed(used); }

    public List<Code> saveCodes(List<Code> codes) {
        var saved = codeRepository.saveAll(codes);
        var list = new ArrayList<Code>();
        saved.forEach(list::add);
        return list;
    }

    private List<EmailWrapper> processRequests(List<SheetsRequest> sheetsRequests, boolean send, String competition, String date) {
        var requests = sheetsRequests.stream()
                .map(Request::fromSheetsRequest)
                .toList();

        var codesPerStore = new HashMap<Store, List<Code>>();
        codesPerStore.put(PG, codeRepository.findAllByStoreAndUsedFalse(PG));
        codesPerStore.put(NBDG, codeRepository.findAllByStoreAndUsedFalse(NBDG));

        var usedCodes = new ArrayList<Code>();

        var emails = requests.stream()
                .map(request -> {
                    Store store = null;
                    if(request.getStore().contains("PowerGrip")) {
                        store = PG;
                    }
                    else if (request.getStore().contains("NBDG")) {
                        store = NBDG;
                    }
                    if(store != null) {
                        var codes = codesPerStore.get(store);
                        if(codes.isEmpty()) {
                            return EmailWrapper.builder()
                                    .error(String.format("Not enough codes for %s", store.getStoreName()))
                                    .build();
                        }
                        else {
                            var code = codes.remove(0);
                            usedCodes.add(code);
                            var content = createEmailContentWithTemplate(store, date == null ? request.getDate() : date, code, competition);
                            var email = Email.builder()
                                    .from(EMAIL_FROM)
                                    .to(request.getEmail())
                                    .subject(EMAIL_SUBJECT.replace("<COMPETITION>", competition))
                                    .content(content)
                                    .build();
                            return EmailWrapper.builder()
                                    .email(email)
                                    .build();
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();

        if(send) {
            usedCodes.forEach(this::setCodeUsed);
            emails.stream()
                    .map(EmailWrapper::getEmail)
                    .filter(Objects::nonNull)
                    .map(gmailAPI::sendEmail)
                    .filter(Objects::nonNull)
                    .forEach(this::markSent);
        }

        return emails;
    }

    public Report report(TemporalAccessor from, TemporalAccessor to) {
        var fromLocalDate = LocalDate.from(from);
        var toLocalDate = LocalDate.from(to);
        var sentEmails = sentRepository.findAllByTimestampBetween(
                ZonedDateTime.of(fromLocalDate, LocalTime.MIN, ZoneId.systemDefault()),
                ZonedDateTime.of(toLocalDate, LocalTime.MIN, ZoneId.systemDefault()));
        var codesRemaining = new HashMap<Store, Integer>();
        for(var store : Store.values()) {
            var unusedCodes = codeRepository.findAllByStoreAndUsedFalse(store);
            codesRemaining.put(store, unusedCodes.size());
        }
        return Report.builder()
                .emails(sentEmails.stream().map(ReportEmail::fromSent).toList())
                .codesRemaining(codesRemaining)
                .build();
    }

    private String createEmailContentWithTemplate(Store store, String date, Code code, String competition) {
        return EMAIL_TEMPLATE
                .replace("<DATE>", date)
                .replace("<COMPETITION>", competition)
                .replace("<CODE>", code.getCode())
                .replace("<STORE>", store.getEmailName())
                .replace("<VALID>", code.getValid());
    }

    private void setCodeUsed(Code code) {
        code.setUsed(true);
        codeRepository.save(code);
    }

    private void markSent(Email email) {
        var sent = Sent.fromEmail(email);
        sentRepository.save(sent);
    }

    public List<SheetsRequest> getNewSheetsRequests() {
        try {
            return sheetsAPI.getRequests().stream().sorted(compareRequests).toList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<SheetsRequest> getSheetRequests(SheetsRequest.Status status) {
        return status == null
                ? sheetsRequestRepository.findAll().stream().sorted(compareRequests).toList()
                : sheetsRequestRepository.findByStatus(status).stream().sorted(compareRequests).toList();
    }

    public List<EmailWrapper> completeSheetsRequests(List<Long> ids, boolean send, String competition, String date) {
        var requests = sheetsRequestRepository.findByIdInAndStatus(ids, REQUESTED);

        if(requests.size() != ids.size()) {
            var error = EmailWrapper.builder()
                    .error("Number of ids and requests don't match")
                    .build();
            return List.of(error);
        }

        var processedRequests = processRequests(requests, send, competition, date);

        if(send) {
            requests.forEach(request -> request.setStatus(COMPLETED));
            sheetsRequestRepository.saveAll(requests);
        }

        return processedRequests;
    }

    public List<SheetsRequest> rejectSheetsRequests(List<Long> ids) {
        var requests = sheetsRequestRepository.findByIdIn(ids);

        if(requests.size() != ids.size()) {
            System.out.println("Number of ids and requests don't match");
            return null;
        }

        requests.forEach(request -> {
            if(request.getStatus() == REQUESTED) {
                request.setStatus(REJECTED);
            } else {
                System.out.printf("Invalid status change from %s to REJECTED\n", request.getStatus());
            }
        });
        sheetsRequestRepository.saveAll(requests);

        return sheetsRequestRepository.findByIdIn(ids);
    }
}
