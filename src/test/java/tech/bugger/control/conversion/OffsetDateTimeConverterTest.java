package tech.bugger.control.conversion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class OffsetDateTimeConverterTest {

    private OffsetDateTimeConverter offsetDateTimeConverter;

    @Mock
    private ResourceBundle labelsBundle;

    @BeforeEach
    public void setUp() {
        offsetDateTimeConverter = new OffsetDateTimeConverter(labelsBundle);
    }

    @Test
    public void testGetAsObject() {
        assertThrows(UnsupportedOperationException.class, () -> offsetDateTimeConverter.getAsObject(null, null, null));
    }

    @Test
    public void testGetAsString() {
        doReturn("yyyy-MM-dd HH:mm Z").when(labelsBundle).getString("date_time_pattern");
        OffsetDateTime dateTime = OffsetDateTime.of(LocalDate.of(2020, 12, 31),
                                                    LocalTime.of(14, 42, 7),
                                                    ZoneOffset.ofHours(1));
        assertEquals("2020-12-31 14:42 +0100", offsetDateTimeConverter.getAsString(null, null, dateTime));
    }

}