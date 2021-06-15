package co.uk.diwise.acceptance

import co.uk.diwise.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.*
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
import io.cucumber.datatable.DataTable
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class StepDefs {

    private data class Temporal(val asOf: AsOf, val validFrom: ValidFrom ) : FlightTemporal {
        override fun asOf(): AsOf = asOf
        override fun validFrom(): ValidFrom = validFrom
        fun next(): Temporal = Temporal(asOf.plusHours(1), validFrom.next())
    }

    private val JSON = ObjectMapper().apply {
        registerKotlinModule()
        registerModule(JavaTimeModule())
    }

    private lateinit var tower: FlightControlTower

    @Given("Empty event history")
    fun emptyHistory() {
        tower = FlightControlTower()
    }

    @When("The following event added")
    fun addEvents(rows: DataTable) {
        val maps = rows.asMaps()
        maps.forEach {
            val event = JSON.convertValue<FlightStatusUpdate>(it)
            tower.addFlightEvent(event)
        }
    }

    @When("The following event has been removed")
    fun removeEvents(rows: DataTable) {
        val maps = rows.asMaps()
        maps.forEach {
            val event = JSON.convertValue<FlightStatusUpdate>(it)
            tower.cancelEvent(event)
        }
    }

    @Then("Flight status at {string} should be")
    fun checkFlightStatus(asOF: String, status: DataTable) {
        val asOf = LocalDateTime.parse(asOF, DateTimeFormatter.ofPattern(FlightStatusUpdate.DATE_FMT) )
        val buf = ByteArrayOutputStream()
        PrintWriter( buf ).use { tower.getStatus( it, asOf ) }
        val gotStatus = buf.toString()
        val lines = gotStatus.split("\n")
        val wantLines = status.asList()
        val l = if (lines.last().isEmpty()) lines.subList(0, lines.size-1) else lines
        assertThat(l.size, equalTo(wantLines.size))
        wantLines.withIndex().forEach{
            assertThat("At line ${it.index+1}", l.get(it.index), equalTo(it.value))
        }

    }
}