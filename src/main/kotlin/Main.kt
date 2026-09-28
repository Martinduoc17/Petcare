import java.time.LocalDateTime

fun main() {
    val petCare = PetCare()
    petCare.iniciar()

    val max = Canino(
        codigoAtencion = "CA12CD",
        nombre = "Max",
        especie = "Golden Retriever",
        fechaIngreso = LocalDateTime.now(),
        tipoDueno = "convenio"
    )

    val luna = Canino(
        codigoAtencion = "CA99ZA",
        nombre = "Luna",
        especie = "Labrador",
        fechaIngreso = LocalDateTime.now(),
        tipoDueno = "particular"
    )

    val misi = Felino(
        codigoAtencion = "FE22TO",
        nombre = "Misi",
        especie = "Siamés",
        fechaIngreso = LocalDateTime.now(),
        tipoDueno = "particular"
    )

    val loro = Exotico(
        codigoAtencion = "EX44RG",
        nombre = "Loro",
        especie = "Amazónico",
        fechaIngreso = LocalDateTime.now(),
        tipoDueno = "municipal",
        silvestre = true
    )

    val iguana = Exotico(
        codigoAtencion = "EX77RG",
        nombre = "Iguana",
        especie = "Verde",
        fechaIngreso = LocalDateTime.now(),
        tipoDueno = "particular",
        silvestre = false
    )

    // Se registran entradas usando una colección de objetos Paciente.
    val pacientes: List<Paciente> = listOf(max, luna, misi, loro, iguana)

    println("\n========== REGISTRO DE ENTRADAS ==========")
    pacientes.forEach { paciente ->
        petCare.registrarEntrada(paciente)
    }

    petCare.mostrarBoxes()

    println("\n========== CONSULTAS ==========")
    petCare.consultarBoxesDisponibles()
    petCare.mostrarPacientesEnAtencion()

    println("\n========== SALIDAS ==========")

    // Tiempos sugeridos por el caso de estudio.
    petCare.registrarSalida("CA12CD", 75)
    petCare.registrarSalida("CA99ZA", 180)
    petCare.registrarSalida("FE22TO", 18)
    petCare.registrarSalida("EX44RG", 120)
    petCare.registrarSalida("EX77RG", 45)

    println("\n========== CONSULTAS DEL TURNO ==========")
    petCare.consultarConvenio()
    petCare.consultarPromedio()
    petCare.consultarCodigosFinalizados()
    petCare.consultarMayorTiempo()
    petCare.mostrarRecaudacionPorTipo()
    petCare.consultarBoxesDisponibles()

    println("\n========== PRUEBA DE ERROR ==========")

    val pacienteInvalido = Canino(
        codigoAtencion = "123ABC",
        nombre = "Paciente Error",
        especie = "Desconocida",
        fechaIngreso = LocalDateTime.now(),
        tipoDueno = "particular"
    )

    petCare.registrarEntrada(pacienteInvalido)

    // Paciente inexistente para demostrar que el programa continúa.
    petCare.registrarSalida("ZZ99ZZ", 30)

    println("\n========== REPORTE FINAL ==========")
    petCare.reporteCierre()
}
