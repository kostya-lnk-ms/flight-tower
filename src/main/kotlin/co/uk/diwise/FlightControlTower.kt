package co.uk.diwise

import java.io.PrintWriter

class FlightControlTower {

    data class Temporal(val asOf: AsOf, val validFrom: ValidFrom ) : FlightTemporal {
        override fun asOf(): AsOf = asOf
        override fun validFrom(): ValidFrom = validFrom
    }

    private val keyFunc = FlightStatusUpdate::planeID
    private val tempHist = TemporalHistory<Temporal, String, FlightStatusUpdate>(keyFunc)
    private val epoch = AtomicValidFrom(0)

    fun addFlightEvent(event: FlightStatusUpdate) {
        val vf = epoch.incrementAndGet()
        tempHist.add( Temporal(asOf = event.timestamp, validFrom = vf), event )
    }

    fun cancelEvent(event: FlightStatusUpdate) {
        val vf = epoch.incrementAndGet()
        tempHist.cancelFor( Temporal(asOf = event.timestamp, validFrom = vf), keyFunc.invoke(event) )
    }

    fun getStatus(w: PrintWriter, asOf: AsOf) {
        val t = Temporal(asOf = AsOf.MAX, validFrom = epoch.get())
        tempHist.keys( t )
            .sorted()
            .forEach { key ->
                w.print(key)
                FlightStatusProps.all.forEach { prop->
                    val pv = tempHist.getPropValue(t, key, prop)
                    w.print(" $pv")
                }
                w.print("\n")
            }
    }
}