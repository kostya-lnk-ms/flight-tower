package co.uk.diwise

import org.junit.Test
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import java.lang.RuntimeException
import java.time.LocalDate
import java.time.LocalTime

class TemporalHistoryTest {

    data class TstEnt(val key:String, val state: String, val numV: Int)

    private val amount = FoldableProperty(TstEnt::numV, Int::plus )
    private val state  = FoldableProperty(TstEnt::state){_, newS -> newS}
    private val baseAsOf = AsOf.of(LocalDate.of(2002,12,31), LocalTime.of(23, 33, 59) )
    private val baseTemp = Temporal(baseAsOf, ValidFrom.zero())

    data class Temporal(val asOf: AsOf, val validFrom: ValidFrom ) : FlightTemporal {
        override fun asOf(): AsOf = asOf
        override fun validFrom(): ValidFrom = validFrom
        fun next(): Temporal = Temporal(asOf.plusHours(1), validFrom.next())
    }

    @Test
    fun testAdd() {
        val th = TemporalHistory<Temporal, String, TstEnt>( TstEnt::key)
        val t1 = baseTemp.next().also { th.add(it, TstEnt(key = "k1", state = "s1", 23)) }

        assertThat(th.getPropValue(t1, "k1", state), equalTo("s1"))
        assertThat(th.getPropValue(t1, "k1", amount), equalTo(23))
        assertThat(th.keys(t1).toList(), equalTo(listOf("k1")))

        val t2 = t1.next().also { th.add(it, TstEnt(key = "k1", state = "s2", -10)) }
        assertThat(th.getPropValue(t2, "k1", state), equalTo("s2"))
        assertThat(th.getPropValue(t2, "k1", amount), equalTo(13))
        assertThat(th.getPropValue(t1, "k1", state), equalTo("s1"))
        assertThat(th.getPropValue(t1, "k1", amount), equalTo(23))
        assertThat(th.keys(t2).toList(), equalTo(listOf("k1")))

        val t3 = t1.copy(validFrom = th.epoch().next()).also { th.add(it, TstEnt(key = "k2", state = "s3", 33)) }
        assertThat(th.keys(t3).toList(), equalTo(listOf("k1", "k2")))
        assertThat(th.getPropValue(t3, "k2", state), equalTo("s3"))
        assertThat(th.getPropValue(t3, "k2", amount), equalTo(33))

        assertThat(th.getPropValue(t3, "k1", state), equalTo("s1"))
        assertThat(th.getPropValue(t3, "k1", amount), equalTo(23))

        assertThat(th.getPropValue(t2, "k1", state), equalTo("s2"))
        assertThat(th.getPropValue(t2, "k1", amount), equalTo(13))

        assertThat(th.getPropValue(t1, "k1", state), equalTo("s1"))
        assertThat(th.getPropValue(t1, "k1", amount), equalTo(23))
        assertThat(th.keys(t2).toList(), equalTo(listOf("k1")))
    }

    @Test
    fun testCancelFirst() {
        val th = TemporalHistory<Temporal, String, TstEnt>( TstEnt::key)
        val t1 = baseTemp.next().also { th.add(it, TstEnt(key = "k1", state = "s1", 23)) }

        val t2 = t1.copy(validFrom = th.epoch().next()).also { th.cancelFor(it, "k1") }
        assertThat(th.keys(t2).toList(), equalTo(emptyList()))
        assertThat(th.keys(t1).toList(), equalTo(listOf("k1")))

        val t3 = t2.copy(validFrom = th.epoch().next()).also { th.add(it, TstEnt(key = "k1", state = "s2", -10)) }
        assertThat(th.getPropValue(t3, "k1", state), equalTo("s2"))
        assertThat(th.getPropValue(t3, "k1", amount), equalTo(-10))
        assertThat(th.keys(t2).toList(), equalTo(emptyList()))
        assertThat(th.keys(t1).toList(), equalTo(listOf("k1")))
        assertThat(th.keys(t3).toList(), equalTo(listOf("k1")))
    }

    @Test
    fun testCancelMiddle() {
        val th = TemporalHistory<Temporal, String, TstEnt>( TstEnt::key)

        val t1 = baseTemp.next().also { th.add(it, TstEnt(key = "k1", state = "s1", 23)) }
        val t2 = t1.copy(validFrom = th.epoch().next()).also { th.cancelFor(it, "k1") }
        val t3 = t1.copy(validFrom = th.epoch().next()).also { th.add(it, TstEnt(key = "k1", state = "s3", 33)) }

        assertThat(th.getPropValue(t3, "k1", state), equalTo("s3"))
        assertThat(th.getPropValue(t3, "k1", amount), equalTo(33))
        assertThat(th.keys(t1).toList(), equalTo(listOf("k1")))
        assertThat(th.keys(t2).toList(), equalTo(listOf()))
        assertThat(th.keys(t3).toList(), equalTo(listOf("k1")))
    }

    @Test(expected = RuntimeException::class)
    fun testCancelLast() {
        val th = TemporalHistory<Temporal, String, TstEnt>( TstEnt::key)

        val t1 = baseTemp.next().also { th.add(it, TstEnt(key = "k1", state = "s1", 23)) }
        assertThat(th.keys(t1).toList(), equalTo(listOf("k1")))

        val t2 = t1.copy(validFrom = th.epoch().next()).also { th.cancelFor(it, "k1") }
        assertThat(th.keys(t2).toList(), equalTo(emptyList()))
        th.getPropValue(t2, "k1", state)
    }
}