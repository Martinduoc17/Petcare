class Box(val numero: Int) {
    var estado: EstadoBox = EstadoBox.Libre
        private set

    fun estaDisponible(): Boolean = estado is EstadoBox.Libre

    fun iniciarProceso(motivo: String): Boolean {
        if (!estaDisponible()) {
            return false
        }

        estado = EstadoBox.EnProceso(motivo)
        return true
    }

    fun asignarPaciente(codigoAtencion: String): Boolean {
        if (estado !is EstadoBox.EnProceso) {
            return false
        }

        estado = EstadoBox.EnAtencion(codigoAtencion)
        return true
    }

    fun liberar(): Boolean {
        if (estado !is EstadoBox.EnAtencion) {
            return false
        }

        estado = EstadoBox.Libre
        return true
    }

    fun dejarFueraDeServicio(motivo: String): Boolean {
        if (!estaDisponible()) {
            return false
        }

        estado = EstadoBox.FueraDeServicio(motivo)
        return true
    }

    fun estadoTexto(): String {
        return when (val actual = estado) {
            EstadoBox.Libre -> "Libre"
            is EstadoBox.EnAtencion -> "EnAtencion (${actual.codigoAtencion})"
            is EstadoBox.EnProceso -> "EnProceso (${actual.motivo})"
            is EstadoBox.FueraDeServicio -> "FueraDeServicio (${actual.motivo})"
        }
    }
}
