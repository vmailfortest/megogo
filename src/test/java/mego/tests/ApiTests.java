package mego.tests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import mego.model.Program;
import mego.model.Time;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApiTests {

    protected ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Test
    public void timeIsCorrect() {
        Object response = BaseSpec.spec().request()
                .get("/time")
                .then()
                .log().body()
                .statusCode(200)
                .extract().body().path("data");

        Time timeResponse = mapper.convertValue(response, Time.class);

        long offsetExpected = ZonedDateTime.now(ZoneId.of("Europe/Kiev")).getOffset().getTotalSeconds();
        long utcExpected = Instant.now().getEpochSecond();
        long localExpected = utcExpected + offsetExpected;

        // Assert
        assertTrue(Math.abs(timeResponse.timestamp - utcExpected) < 10);
        assertTrue(Math.abs(timeResponse.timestamp_gmt - utcExpected) < 10);
        assertTrue(Math.abs(timeResponse.timestamp_local - localExpected) < 10);
        assertEquals(offsetExpected, timeResponse.utc_offset);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1639111", "1585681", "1639231"})
    public void programsSortedByDate(String id) {

        List<Object> response = BaseSpec.spec().request()
                .get("channel?video_ids={id}", id)
                .then()
                .log().body()
                .statusCode(200)
                .extract().body().path("data.programs[0]");

        List<Program> programs = mapper.convertValue(response, new TypeReference<List<Program>>() {
        });

        boolean isSorted = true;
        for (int i = 0; i < programs.size() - 1; i++) {
            if (programs.get(i).start_timestamp > programs.get(i + 1).start_timestamp) {
                isSorted = false;
                break;
            }
        }

        assertTrue(isSorted);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1639111", "1585681", "1639231"})
    public void hasProgramNow(String id) {

        List<Object> response = BaseSpec.spec().request()
                .get("channel?video_ids={id}", id)
                .then()
                .log().body()
                .statusCode(200)
                .extract().body().path("data.programs[0]");

        List<Program> programs = mapper.convertValue(response, new TypeReference<List<Program>>() {
        });

        long currentTime = Instant.now().getEpochSecond();

        boolean hasProgramNow = false;
        if (programs.get(0).start_timestamp <= currentTime && programs.get(0).end_timestamp >= currentTime) {
            hasProgramNow = true;
        }

        assertTrue(hasProgramNow);

    }

    @ParameterizedTest
    @ValueSource(strings = {"1639111", "1585681", "1639231"})
    public void correctPeriod(String id) {

        List<Object> response = BaseSpec.spec().request()
                .get("channel?video_ids={id}", id)
                .then()
                .log().body()
                .statusCode(200)
                .extract().body().path("data.programs[0]");

        List<Program> programs = mapper.convertValue(response, new TypeReference<List<Program>>() {
        });

        long currentTime = Instant.now().getEpochSecond();

        boolean correctPeriod = true;
        for (Program program : programs) {
            if (program.end_timestamp < currentTime
                    || program.start_timestamp > (currentTime + 24 * 3600)) {
                correctPeriod = false;
                break;
            }
        }

        assertTrue(correctPeriod);
    }

}
