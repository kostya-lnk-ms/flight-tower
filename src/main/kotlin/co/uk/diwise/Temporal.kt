package co.uk.diwise

import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.atomic.AtomicLong

interface Temporal<T1 : Comparable<T1>,T2: Comparable<T2> > {
    fun asOf():T1
    fun validFrom():T2
}

typealias AsOf = LocalDateTime
typealias ValidFrom = Long
typealias AtomicValidFrom = AtomicLong

fun Long.Companion.zero():ValidFrom = 0
fun ValidFrom.next():ValidFrom = this + 1
fun ValidFrom.validAt(t: ValidFrom) = this <= t

// define and use concrete types, so to simplify typing further
interface FlightTemporal : Temporal<AsOf, ValidFrom>

class FoldableProperty<V: Any, E: Any>private constructor(private val access:(E)->V,
                                                                     private val combine:(V, V)->V,
                                                                     private val value: V?
    ) {
    constructor(access:(E)->V, combine:(V, V)->V): this(access, combine, null)

    fun value():V = value ?: error("Not initialized")

    fun apply(t: E): FoldableProperty<V, E> {
        return if (value == null) {
            FoldableProperty(access, combine, access.invoke(t) )
        } else {
            FoldableProperty(access, combine, combine.invoke(value, access.invoke(t) ))
        }
    }
}

class TemporalHistory<T: FlightTemporal, K: Any, V: Any>(private val pkExtract:(V)->K) {

    private class ValidFromVal<V>(val from: ValidFrom, val value: V?) : Comparable<ValidFromVal<V>> {
        override fun compareTo(other: ValidFromVal<V>): Int = other.from.compareTo(from)
        override fun toString(): String {
            return "ValidFromVal(from=$from, value=$value)"
        }
    }

    private class TempEnt<V: Any>(
        val history: ConcurrentSkipListMap<AsOf, ConcurrentSkipListSet< ValidFromVal<V> >> = ConcurrentSkipListMap()

    ) {
        override fun toString(): String {
            return "TempEnt(history=$history)"
        }
    }

    private val epoch = AtomicValidFrom(0)
    private val tempDb = ConcurrentHashMap<K, TempEnt<V> >()

    fun epoch(): ValidFrom = epoch.get()

    fun add(tc: T, value: V) {
        val pk = pkExtract.invoke(value)
        val ent = tempDb.computeIfAbsent(pk){ TempEnt() }
        val eh = ent.history.computeIfAbsent(tc.asOf()){ ConcurrentSkipListSet() }
        doAdd(eh, tc, value)
    }

    fun cancelFor(tc: T, key: K) {
        val ent = tempDb[key] ?: error("No record with key $key")
        val eh = ent.history[ tc.asOf() ] ?: error("No valid record at ${tc.asOf()}")
        doAdd(eh, tc, null)
    }

    private fun doAdd(eh: ConcurrentSkipListSet<ValidFromVal<V>>, tc: T, value: V?) {
        eh.add(ValidFromVal(tc.validFrom(), value))
        val e = epoch.get()
        if (tc.validFrom().compareTo(e) > 0) {
            epoch.compareAndExchange(e, tc.validFrom())
        }
    }

    fun keys(tc: T): Sequence<K> {
        return tempDb.entries.asSequence()
            .filter { (_, ent) -> validEntries(ent, tc).firstOrNull() != null }
            .map{ it.key }
    }

    fun <VT: Any> getPropValue(tc: T, key: K, p: FoldableProperty<VT, V> ): VT {
        // TODO: there should be some caching?
        val ent = tempDb[key] ?: error("No record with key $key")
        val pv = validEntries(ent, tc).fold(p){pv,t -> pv.apply(t.value!!) }

        return pv.value()
    }

    private fun validEntries(ent: TempEnt<V>, tc: T) =
        // here we flatten the history and filter by <= asOF, <= validFrom
        // and skip effective nulls e.g. cancelled values
        ent.history.headMap(tc.asOf(), true)
        .asSequence()
        .flatMap { itemHist->
            itemHist.value.asSequence()
                .firstOrNull { it.from.validAt(tc.validFrom()) }
                ?.takeIf { it.value != null }
                ?.let { sequenceOf(it) } ?: emptySequence()
        }
}