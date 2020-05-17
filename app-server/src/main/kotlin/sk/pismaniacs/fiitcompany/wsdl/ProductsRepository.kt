package sk.pismaniacs.fiitcompany.wsdl

import sk.pismaniacs.fiitcompany.model.Item
import sk.stuba.fiit.predmety.pis.pis.students.team022produkt.Team022ProduktService
import sk.stuba.fiit.predmety.pis.pis.students.team022produkt.types.*
import kotlin.random.Random

object ProductsRepository {
    private var produktyPort = Team022ProduktService().team022ProduktPort

    fun insertProducts(items: List<Item>){
        produktyPort
                .getAll(GetAll())
                .produkts
                .produkt.forEach {produkt ->
            produktyPort.delete(Delete().apply {
                teamId = "022"
                teamPassword = "Y6ZSLR"
                entityId = produkt.id
            })
        }
        items.forEach {item ->
            produktyPort.insert(Insert().apply {
                teamId = "022"
                teamPassword = "Y6ZSLR"
                produkt = Produkt().apply {
                    name = item.name
                    pocetNaSklade = 0
                }
            })
        }
    }

    fun updateProductQuantities(){
        produktyPort.getAll(GetAll()).produkts.produkt.forEach {item->
            produktyPort.update(Update().apply {
                teamId = "022"
                teamPassword = "Y6ZSLR"
                entityId = item.id
                produkt = Produkt().apply {
                    name = item.name
                    pocetNaSklade = Random.nextInt(0, 100)
                }
            })
        }
    }

    fun getIdsToOrder() = produktyPort
            .getAll(GetAll())
            .produkts
            .produkt
            .filter { it.pocetNaSklade == 0 }
            .map { it.name }
}