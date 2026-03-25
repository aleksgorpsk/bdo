package ag.com.dbo.models;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;


@Converter
public class UnixTimestampConverter implements AttributeConverter<LocalDate, Long> {

    @Override
    public Long convertToDatabaseColumn(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
    }

    @Override
    public LocalDate convertToEntityAttribute(Long timestamp) {
        if (timestamp == null) {
            return null;
        }
        return Instant.ofEpochSecond(timestamp).atZone(ZoneOffset.UTC).toLocalDate();
    }
}
