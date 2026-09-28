import java.time.LocalDateTime

class Felino(
    codigoAtencion: String,
    nombre: String,
    especie: String,
    fechaIngreso: LocalDateTime,
    tipoDueno: String
) : Paciente(codigoAtencion, nombre, especie, fechaIngreso, tipoDueno) {

    override fun tarifaBasePorHora(): Double = 9000.0

    override fun calcularCostoTiempo(minutos: Long): Double {
        // Menos de 20 minutos significa cobro $0 según el enunciado.
        if (minutos < 20) {
            return 0.0
        }
        return super.calcularCostoTiempo(minutos)
    }
}
