import java.time.LocalDateTime

class Exotico(
    codigoAtencion: String,
    nombre: String,
    especie: String,
    fechaIngreso: LocalDateTime,
    tipoDueno: String,
    val silvestre: Boolean
) : Paciente(codigoAtencion, nombre, especie, fechaIngreso, tipoDueno) {

    override fun tarifaBasePorHora(): Double = 20000.0

    override fun calcularCostoTiempo(minutos: Long): Double {
        val costo = super.calcularCostoTiempo(minutos)

        // Si es silvestre, se agrega un recargo del 30%.
        return if (silvestre) {
            costo * 1.30
        } else {
            costo
        }
    }

    override fun detalle(): String {
        return super.detalle() + " | Silvestre: ${if (silvestre) "Sí" else "No"}"
    }
}
