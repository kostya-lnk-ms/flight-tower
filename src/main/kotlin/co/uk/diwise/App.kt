package co.uk.diwise

import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import java.lang.Exception
import java.time.format.DateTimeFormatter
import java.util.*

fun main(args: Array<String>) {
    println("""Usage:
            To add event:
            A>F222,747,DUBLIN,LONDON,Re-Fuel,2021-03-29T10:00:00,200
              Adding an event with the same time and plane ID would create an update
            To cancel event:
            C>F222,747,DUBLIN,LONDON,Re-Fuel,2021-03-29T10:00:00,200
            To display current status:
            S>2021-03-29T10:00:00
        """)
    val tower = FlightControlTower()
    val input = Scanner(System.`in`)
    System.out.println("Waiting for console input...")
    while (input.hasNext()) {
        val line = input.nextLine()
        if (line.length < 2) {
            System.err.println("Unrecognized input $line")
            continue
        }
        val cmd = line.substring(0..1)
        val arg = line.substring(2)
        try {
            when (cmd) {
                "A>" -> {
                    tower.addFlightEvent(FlightStatusUpdate.fromString(arg))
                    println("Added")
                }
                "C>" -> {
                    tower.cancelEvent(FlightStatusUpdate.fromString(arg))
                    System.out.println("Cancelled")
                }
                "S>" -> {
                    System.out.println("Current status")
                    val asOf = AsOf.parse(arg, DateTimeFormatter.ofPattern(FlightStatusUpdate.DATE_FMT))
                    val buf = ByteArrayOutputStream()
                    PrintWriter(buf).use { tower.getStatus(it, asOf) }
                    System.out.println(buf.toString())
                }
            }
        } catch (ex: Exception) {
            System.err.println("Error: $ex")
        }
        System.out.println("Waiting for console input...")
        System.out.flush()
    }
}
