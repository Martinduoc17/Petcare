import java.time.LocalDateTime

class Canino(
    codigoAtencion: String,
    nombre: String,
    especie: String,
    fechaIngreso: LocalDateTime,
    tipoDueno: String
) : Paciente(codigoAtencion, nombre, especie, fechaIngreso, tipoDueno) {

    override fun tarifaBasePorHora(): Double = 12000.0

    override fun calcularCostoTiempo(minutos: Long): Double {
        val costo = super.calcularCostoTiempo(minutos)

        // En caninos, el convenio aplica un 20% de descuento al costo por tiempo.
        return if (tipoDueno.equals("convenio", ignoreCase = true)) {
            costo * 0.80
        } else {
            costo
        }
    }
}
