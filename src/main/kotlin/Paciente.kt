import java.time.LocalDateTime
import java.util.Locale

open class Paciente(
    val codigoAtencion: String,
    val nombre: String,
    val especie: String,
    val fechaIngreso: LocalDateTime,
    val tipoDueno: String
) {
    open fun tarifaBasePorHora(): Double = 0.0

    open fun calcularCostoTiempo(minutos: Long): Double {
        val horas = minutos.toDouble() / 60.0
        return horas * tarifaBasePorHora()
    }

    open fun detalle(): String {
        return "$codigoAtencion | $nombre | $especie | Dueño: ${tipoDueno.replaceFirstChar { it.titlecase(Locale.getDefault()) }}"
    }
}
