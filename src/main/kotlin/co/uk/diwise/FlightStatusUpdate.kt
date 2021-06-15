package co.uk.diwise

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.*
import java.math.BigDecimal
import java.time.LocalDateTime

typealias PlaneId = String
typealias PlaneModel = String
typealias PlanePoint = String

enum class PlaneStatus {
    Landed,
    @JsonProperty("Awaiting-Takeoff")
    AwaitingTakeoff,
    @JsonProperty("Take-Off")
    TakeOff,
    @JsonProperty("In-Flight")
    InFlight,
    @JsonProperty("Re-Fuel")
    ReFuel,
    ;


    companion object {
        fun displayString(s: PlaneStatus): String =
            when (s) {
                Landed -> "Landed"
                AwaitingTakeoff -> "Awaiting-Takeoff"
                TakeOff -> "Take-Off"
                InFlight -> "In-Flight"
                ReFuel -> "Re-Fuel"
            }
    }

    override fun toString(): String {
        return displayString(this)
    }
}

@JsonPropertyOrder(value=["planeID","planeModel","origin","destination","eventType", "timestamp","fuelDelta"])
data class FlightStatusUpdate(
    val planeID: PlaneId,
    val planeModel: PlaneModel,
    val origin: PlanePoint,
    val destination: PlanePoint,
    val eventType: PlaneStatus,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_FMT)
    val timestamp: LocalDateTime,
    val fuelDelta: BigDecimal
) {
    companion object {
        const val DATE_FMT = "yyyy-MM-dd'T'HH:mm:ss"
        private const val COLUMN_SEPARATOR=','
        private val PARSER = CsvMapper().apply {
            registerKotlinModule()
            registerModule(JavaTimeModule())
        }
        private val SCHEMA: CsvSchema = PARSER
            .schemaFor(FlightStatusUpdate::class.java)
            .withColumnSeparator(COLUMN_SEPARATOR)

        /*
        The original format specified in the specification can be ambiguous to parse -
        For example, plane going from 'Tel Aviv' to 'New York'
        Which could be read as 'Tel'->'Aviv New York'
        The format should've used a separator, different to characters used in city names or have
        specified some sort of 'escape' (disambiguation) technique
         */
        fun fromString(line: String) : FlightStatusUpdate {
            val reader = PARSER
                .readerFor(FlightStatusUpdate::class.java)
                .with( SCHEMA )

            return reader.readValue(line)
        }
    }
}

