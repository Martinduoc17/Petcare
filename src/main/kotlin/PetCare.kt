import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PetCare {
    private val boxes = List(10) { Box(it + 1) }

    // Historial solamente de pacientes que finalizaron su atención durante el turno.
    private val historial = mutableListOf<Paciente>()

    private val tickets = mutableListOf<Ticket>()

    private var recaudacionTotal = 0.0
    private var recaudacionCanino = 0.0
    private var recaudacionFelino = 0.0
    private var recaudacionExotico = 0.0
    private var siguienteTicket = 1

    private val pacientesEnAtencion = mutableMapOf<String, Paciente>()

    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")

    fun iniciar() {
        println("==========================================")
        println("           PETCARE - VETERINARIA")
        println("==========================================")
        println("Sistema inicializado con 10 boxes.")
        println()
    }

    fun registrarEntrada(paciente: Paciente) {
        try {
            validarPaciente(paciente)

            if (pacientesEnAtencion.containsKey(paciente.codigoAtencion)) {
                throw IllegalArgumentException("Ya existe un paciente con ese código en atención.")
            }

            val box = boxes.firstOrNull { it.estaDisponible() }
                ?: throw IllegalStateException("El sistema está lleno: no hay boxes disponibles.")

            runBlockingEntrada(box, paciente)
        } catch (e: IllegalArgumentException) {
            println("ERROR DE DATOS: ${e.message}")
        } catch (e: IllegalStateException) {
            println("ERROR DE OPERACIÓN: ${e.message}")
        }
    }

    private fun validarPaciente(paciente: Paciente) {
        val regex = Regex("^[A-Za-z]{2}\\d{2}[A-Za-z]{2}$")

        if (!regex.matches(paciente.codigoAtencion)) {
            throw IllegalArgumentException(
                "El código de atención '${paciente.codigoAtencion}' es inválido."
            )
        }

        val tiposValidos = setOf("particular", "convenio", "municipal")
        if (paciente.tipoDueno.lowercase() !in tiposValidos) {
            throw IllegalArgumentException(
                "El tipo de dueño '${paciente.tipoDueno}' es inválido."
            )
        }

        if (paciente.tarifaBasePorHora() <= 0) {
            throw IllegalArgumentException("La tarifa base debe ser mayor que cero.")
        }
    }

    private fun runBlockingEntrada(box: Box, paciente: Paciente) {
        kotlinx.coroutines.runBlocking {
            val resultado = registrarEntradaAsincrona(box, paciente)

            when (resultado) {
                EstadoOperacion.Procesando -> {
                    println("Estado inesperado: operación aún en proceso.")
                }

                is EstadoOperacion.Exito -> {
                    pacientesEnAtencion[paciente.codigoAtencion] = paciente
                    println(resultado.mensaje)
                }

                is EstadoOperacion.Error -> {
                    println("ERROR: ${resultado.mensaje}")
                }
            }
        }
    }

    private suspend fun registrarEntradaAsincrona(
        box: Box,
        paciente: Paciente
    ): EstadoOperacion {
        if (!box.estaDisponible()) {
            return EstadoOperacion.Error("El box ${box.numero} no está disponible.")
        }

        box.iniciarProceso("Registrando entrada")
        println("Box ${box.numero}: EnProceso - registrando entrada.")
        delay(3000)

        box.asignarPaciente(paciente.codigoAtencion)

        return EstadoOperacion.Exito(
            "Entrada confirmada. Paciente ${paciente.nombre} asignado al box ${box.numero}."
        )
    }

    fun registrarSalida(codigoAtencion: String, minutos: Long) {
        try {
            if (minutos < 0) {
                throw IllegalArgumentException("El tiempo de uso no puede ser negativo.")
            }

            val paciente = pacientesEnAtencion[codigoAtencion]
                ?: throw NoSuchElementException(
                    "No se encontró el paciente con código $codigoAtencion."
                )

            val box = boxes.firstOrNull {
                it.estado is EstadoBox.EnAtencion &&
                    (it.estado as EstadoBox.EnAtencion).codigoAtencion == codigoAtencion
            } ?: throw IllegalStateException(
                "No se encontró el box asociado al paciente."
            )

            runBlockingSalida(box, paciente, minutos)
        } catch (e: IllegalArgumentException) {
            println("ERROR DE DATOS: ${e.message}")
        } catch (e: NoSuchElementException) {
            println("PACIENTE NO ENCONTRADO: ${e.message}")
        } catch (e: IllegalStateException) {
            println("ERROR DE OPERACIÓN: ${e.message}")
        }
    }

    private fun runBlockingSalida(
        box: Box,
        paciente: Paciente,
        minutos: Long
    ) {
        kotlinx.coroutines.runBlocking {
            val resultado = procesarSalidaAsincrona(box, paciente, minutos)

            when (resultado) {
                EstadoOperacion.Procesando -> {
                    println("Estado inesperado: operación aún en proceso.")
                }

                is EstadoOperacion.Exito -> {
                    finalizarPaciente(box, paciente, minutos)
                    println(resultado.mensaje)
                }

                is EstadoOperacion.Error -> {
                    println("ERROR: ${resultado.mensaje}")
                }
            }
        }
    }

    private suspend fun procesarSalidaAsincrona(
        box: Box,
        paciente: Paciente,
        minutos: Long
    ): EstadoOperacion {
        if (box.estado !is EstadoBox.EnAtencion) {
            return EstadoOperacion.Error("El box no está en estado EnAtencion.")
        }

        box.iniciarProceso("Calculando tarifa")
        println("Box ${box.numero}: EnProceso - calculando tarifa.")
        delay(6500)

        val monto = calcularMontoFinal(paciente, minutos)

        if (monto <= 0.0) {
            // El caso felino menor a 20 minutos produce $0 por regla del negocio.
            // Se informa como resultado inválido para la regla general de tarifa.
            return EstadoOperacion.Error(
                "El resultado de tarifa es inválido: monto $${formatearMonto(monto)}."
            )
        }

        return EstadoOperacion.Exito("Salida procesada correctamente.")
    }

    private fun calcularMontoFinal(paciente: Paciente, minutos: Long): Double {
        val costoTiempo = paciente.calcularCostoTiempo(minutos)

        // Primero se calcula el costo según el tipo de paciente.
        // Luego se aplica IVA del 19%.
        var montoConIva = costoTiempo * 1.19

        // Finalmente, si el dueño es municipal, se aplica 50% sobre el monto con IVA.
        if (paciente.tipoDueno.equals("municipal", ignoreCase = true)) {
            montoConIva *= 0.50
        }

        if (montoConIva <= 0.0) {
            throw IllegalArgumentException("La tarifa calculada no puede ser menor o igual a cero.")
        }

        return montoConIva
    }

    private fun finalizarPaciente(
        box: Box,
        paciente: Paciente,
        minutos: Long
    ) {
        val monto = calcularMontoFinal(paciente, minutos)

        val ticket = Ticket(
            numero = siguienteTicket++,
            tipo = paciente::class.simpleName ?: "Paciente",
            codigoAtencion = paciente.codigoAtencion,
            tiempoUsoMinutos = minutos,
            montoPagado = monto
        )

        tickets.add(ticket)
        historial.add(paciente)
        pacientesEnAtencion.remove(paciente.codigoAtencion)

        recaudacionTotal += monto

        when (paciente) {
            is Canino -> recaudacionCanino += monto
            is Felino -> recaudacionFelino += monto
            is Exotico -> recaudacionExotico += monto
        }

        box.liberar()

        println("Ticket #${ticket.numero} emitido.")
        println("Monto pagado: $${formatearMonto(ticket.montoPagado)}")
        println("Box ${box.numero} liberado.")
    }

    fun mostrarBoxes() {
        println("\n========== ESTADO DE BOXES ==========")

        boxes.forEach { box ->
            println("Box ${box.numero}: ${box.estadoTexto()}")
        }
    }

    fun consultarBoxesDisponibles() {
        val disponibles = boxes.count { it.estaDisponible() }
        println("Boxes disponibles en este momento: $disponibles")
    }

    fun consultarConvenio() {
        println("\n====== PACIENTES CLIENTES CONVENIO ======")

        val convenio = historial.filter {
            it.tipoDueno.equals("convenio", ignoreCase = true)
        }

        if (convenio.isEmpty()) {
            println("No existen pacientes de clientes convenio en el historial.")
        } else {
            convenio.forEach {
                println(it.detalle())
            }
        }
    }

    fun consultarPromedio() {
        val promedio = if (tickets.isEmpty()) {
            0.0
        } else {
            tickets.map { it.montoPagado }.average()
        }

        println("Ingreso promedio por paciente: $${formatearMonto(promedio)}")
    }

    fun consultarCodigosFinalizados() {
        println("\n====== CÓDIGOS FINALIZADOS ======")

        val codigos = historial.map { it.codigoAtencion }

        if (codigos.isEmpty()) {
            println("No existen pacientes finalizados.")
        } else {
            codigos.forEach { println(it) }
        }
    }

    fun consultarMayorTiempo() {
        println("\n====== MAYOR TIEMPO DE USO ======")

        val mayor = tickets.maxByOrNull { it.tiempoUsoMinutos }

        if (mayor == null) {
            println("No existen pacientes finalizados.")
        } else {
            println(
                "Código: ${mayor.codigoAtencion} | " +
                    "Tiempo: ${mayor.tiempoUsoMinutos} minutos"
            )
        }
    }

    fun mostrarPacientesEnAtencion() {
        println("\n====== PACIENTES EN ATENCIÓN ======")

        if (pacientesEnAtencion.isEmpty()) {
            println("No hay pacientes actualmente en atención.")
        } else {
            pacientesEnAtencion.values.forEach {
                println(it.detalle())
            }
        }
    }

    fun reporteCierre() {
        println("\n==========================================")
        println("          REPORTE DE CIERRE")
        println("==========================================")

        if (tickets.isEmpty()) {
            println("No hubo pacientes atendidos durante el turno.")
        } else {
            println("\n--- Pacientes atendidos ---")

            tickets.forEach { ticket ->
                println(
                    "Ticket #${ticket.numero} | " +
                        "Tipo: ${ticket.tipo} | " +
                        "Código: ${ticket.codigoAtencion} | " +
                        "Tiempo: ${ticket.tiempoUsoMinutos} min | " +
                        "Monto: $${formatearMonto(ticket.montoPagado)}"
                )
            }
        }

        val cantidad = tickets.size
        val promedio = if (tickets.isEmpty()) 0.0 else tickets.sumOf { it.montoPagado } / cantidad

        val ingresosPorTipo = mapOf(
            "Canino" to recaudacionCanino,
            "Felino" to recaudacionFelino,
            "Exotico" to recaudacionExotico
        )

        val tipoMayorIngreso = ingresosPorTipo.maxByOrNull { it.value }

        val boxesDisponibles = boxes.count { it.estaDisponible() }

        println("\n--- Resumen ---")
        println("Total recaudado: $${formatearMonto(recaudacionTotal)}")
        println("Cantidad de pacientes atendidos: $cantidad")
        println("Ingreso promedio: $${formatearMonto(promedio)}")

        if (tipoMayorIngreso != null) {
            println(
                "Tipo de paciente que más ingresos generó: " +
                    "${tipoMayorIngreso.key} ($${formatearMonto(tipoMayorIngreso.value)})"
            )
        }

        println("Boxes disponibles al cierre: $boxesDisponibles")
        println("Hora del reporte: ${LocalDateTime.now().format(formatter)}")
        println("==========================================")
    }

    fun registrarBoxFueraDeServicio(numero: Int, motivo: String) {
        val box = boxes.getOrNull(numero - 1)

        if (box == null) {
            println("ERROR: El box debe estar entre 1 y 10.")
            return
        }

        if (box.dejarFueraDeServicio(motivo)) {
            println("Box $numero quedó FueraDeServicio: $motivo")
        } else {
            println("ERROR: El box $numero no puede pasar a FueraDeServicio desde su estado actual.")
        }
    }

    fun mostrarRecaudacionPorTipo() {
        println("\n====== RECAUDACIÓN POR TIPO ======")
        println("Canino: $${formatearMonto(recaudacionCanino)}")
        println("Felino: $${formatearMonto(recaudacionFelino)}")
        println("Exótico: $${formatearMonto(recaudacionExotico)}")
    }

    private fun formatearMonto(monto: Double): String {
        return String.format("%,.0f", monto)
    }
}
