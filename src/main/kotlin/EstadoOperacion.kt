sealed class EstadoOperacion {
    data object Procesando : EstadoOperacion()
    data class Exito(val mensaje: String) : EstadoOperacion()
    data class Error(val mensaje: String) : EstadoOperacion()
}
