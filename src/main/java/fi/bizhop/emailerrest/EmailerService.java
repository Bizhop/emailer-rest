package fi.bizhop.emailerrest;

import com.opencsv.bean.CsvToBeanBuilder;
import fi.bizhop.emailerrest.db.CodeRepository;
import fi.bizhop.emailerrest.db.SentRepository;
import fi.bizhop.emailerrest.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static fi.bizhop.emailerrest.model.Store.NBDG;
import static fi.bizhop.emailerrest.model.Store.PG;

@Service
@RequiredArgsConstructor
public class EmailerService {
    final CodeRepository codeRepository;
    final SentRepository sentRepository;
    final GmailAPI gmailAPI;

    private final String EMAIL_FROM = "rahastonhoitaja@discgolfvikings.fi";
    private final String EMAIL_SUBJECT = "Kivikon viikkokisojen lahjakortti";
    private final String EMAIL_TEMPLATE = "Hei,\n\nja onneksi olkoon! Voitit Kivikon viikkokisoissa <DATE> lahjakortin arvoltaan 15€.\n\nOhessa koodi, jonka voit käyttää <STORE>-kauppaan. Koodi on voimassa <VALID> asti.\n\n<CODE> 15.00 €\n\nYstävällisin terveisin:\n\nVille Piispa\n\nDisc Golf Vikings ry:n rahastonhoitaja\nDisc Golf Vikings Viikkokisatiimi\n";

    public List<Code> getCodes() { return codeRepository.findAll();  }

    public List<Code> getCodes(Boolean used) { return codeRepository.findAllByUsed(used); }

    public void saveCodes(List<Code> codes) { codeRepository.saveAll(codes); }

    public List<EmailWrapper> processRequests(String body, boolean send) {
        var requests = new CsvToBeanBuilder<Request>(new StringReader(body))
                .withType(Request.class)
                .build()
                .parse();

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
                            var content = createEmailContentWithTemplate(store, request.getDate(), code);
                            var email = Email.builder()
                                    .from(EMAIL_FROM)
                                    .to(request.getEmail())
                                    .subject(EMAIL_SUBJECT)
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

    private String createEmailContentWithTemplate(Store store, String date, Code code) {
        return EMAIL_TEMPLATE
                .replace("<DATE>", date)
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
}
