package fi.bizhop.emailerrest;

import fi.bizhop.emailerrest.model.Code;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class Controller {
    final CodeRepository codeRepository;

    @RequestMapping(value = "/codes", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody List<Code> listCodes() {
        return codeRepository.findAll();
    }

    @RequestMapping(value = "/codes", method = RequestMethod.POST, consumes = "application/json")
    public void importCodes(@RequestBody List<Code> codes) {
        codeRepository.saveAll(codes);
    }
}
