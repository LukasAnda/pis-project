package sk.pismaniacs.fiitcompany.wsdl

import sk.stuba.fiit.predmety.pis.pis.students.team022objednavka.Team022ObjednavkaService
import sk.stuba.fiit.predmety.pis.pis.students.team022objednavka.types.Insert
import sk.stuba.fiit.predmety.pis.pis.students.team022objednavka.types.Objednavka

object OrderRepository {
    private var objednavkaPort = Team022ObjednavkaService().team022ObjednavkaPort

    fun orderItem(name: String, quantity: Int){
        val response = objednavkaPort.insert(Insert().apply {
            teamId = "022"
            teamPassword = "Y6ZSLR"
            objednavka = Objednavka().apply {
                this.name = name
                pocet = quantity
            }
        })

        println("New request has id: ${response.id}")
    }
}