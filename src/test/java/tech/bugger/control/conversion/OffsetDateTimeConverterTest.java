package tech.bugger.control.conversion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;

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
        ZoneId mezZone = ZoneId.ofOffset("", ZoneOffset.ofHours(1));
        try (MockedStatic<ZoneId> zoneId = mockStatic(ZoneId.class)) {
            zoneId.when(ZoneId::systemDefault).thenReturn(mezZone);
            doReturn("yyyy-MM-dd HH:mm Z").when(labelsBundle).getString("date_time_pattern");
            OffsetDateTime odt = OffsetDateTime.of(LocalDate.of(2020, 12, 31), LocalTime.of(14, 42, 7), ZoneOffset.UTC);
            assertEquals("2020-12-31 15:42 +0100", offsetDateTimeConverter.getAsString(null, null, odt));
        }
    }

}