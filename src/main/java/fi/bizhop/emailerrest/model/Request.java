package fi.bizhop.emailerrest.model;

import com.opencsv.bean.CsvBindByPosition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
public class Request {
    @CsvBindByPosition(position = 0)
    String email;

    @CsvBindByPosition(position = 1)
    String date;

    @CsvBindByPosition(position = 2)
    String store;
}
