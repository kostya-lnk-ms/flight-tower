package co.uk.diwise

import java.math.BigDecimal

object FlightStatusProps {
    val fuelAmount = FoldableProperty(FlightStatusUpdate::fuelDelta, BigDecimal::add)
    val planeStatus= FoldableProperty(FlightStatusUpdate::eventType){_, newS -> newS}

    val all = listOf(planeStatus, fuelAmount)
}