package box.withitem.locationhelper.utils

enum class PointState {

    Base,
    Checked,
    NotRelevant
}

enum class AccidentType {
    Accident,
    Ambulance,
    Breakdown
}

enum class AccidentDescription {
    None,
    CarCollision,
    HittingFooter,
    CollisionWithRider,
    LossOfControl,
    OtherAccidentDescription
}

enum class SeverityAccident {
    None,
    NoInjury,
    Contusion,
    Fracture,
    CriticalSituation,
    Victims,
    UnknownSeverity
}