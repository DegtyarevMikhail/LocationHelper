package box.withitem.locationhelper.utils

fun getAccidentDescription() = mapOf(
    "None" to "Неизвестно",
    "CarCollision" to "Столкновение с авто",
    "HittingFooter" to "Наезд на пешехода",
    "CollisionWithRider" to "Столкновение с райдером",
    "LossOfControl" to "Потеря управления",
    "OtherAccidentDescription" to "Прочее"
)
fun getSeverityAccident()= mapOf(
    "None" to "Неизвестно",
    "NoInjury" to "Без травм",
    "Contusion" to "Ушибы",
    "Fracture" to "Переломы",
    "CriticalSituation" to "Критическое",
    "Victims" to "Есть жертвы",
    "UnknownSeverity" to "Не ясна тяжесть"
)