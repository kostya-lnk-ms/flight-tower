package co.uk.diwise

import org.junit.Test
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class FlightStatusUpdateTest {

    @Test
    fun itShouldParse() {
        val r = FlightStatusUpdate.fromString("F222,747,DUBLIN,LONDON,Re-Fuel,2021-03-29T10:00:00,200")
        assertThat(r.planeID, equalTo("F222"))
        assertThat(r.planeModel, equalTo("747"))
        assertThat(r.origin, equalTo("DUBLIN"))
        assertThat(r.destination, equalTo("LONDON"))
        assertThat(r.eventType, equalTo(PlaneStatus.ReFuel))
        assertThat(r.timestamp, equalTo(LocalDateTime.of(LocalDate.of(2021, 3, 29), LocalTime.of(10, 0, 0) )))
        assertThat(r.fuelDelta, equalTo(BigDecimal.valueOf(200)))
    }

    @Test
    fun itShouldFlyToCitiesWithSpaces() {
        val r = FlightStatusUpdate.fromString("F324,313,LONDON,NEW YORK,Take-Off,2021-03-29T12:00:00,0")
        assertThat(r.planeID, equalTo("F324"))
        assertThat(r.planeModel, equalTo("313"))
        assertThat(r.origin, equalTo("LONDON"))
        assertThat(r.destination, equalTo("NEW YORK"))
        assertThat(r.eventType, equalTo(PlaneStatus.TakeOff))
        assertThat(r.timestamp, equalTo(LocalDateTime.of(LocalDate.of(2021, 3, 29), LocalTime.of(12, 0, 0) )))
        assertThat(r.fuelDelta, equalTo(BigDecimal.valueOf(0)))
    }
}