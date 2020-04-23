package sk.pismaniacs.fiitcompany.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import sk.pismaniacs.fiitcompany.model.Item
import sk.pismaniacs.fiitcompany.model.Purchase
import sk.pismaniacs.fiitcompany.repository.ItemRepository
import sk.pismaniacs.fiitcompany.repository.OrderRepository
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.random.Random

@RestController
@RequestMapping("/items")
class ItemController {
    @Autowired
    lateinit var itemRepository: ItemRepository


    @Autowired
    lateinit var orderRepository: OrderRepository

    @RequestMapping("/setup")
    fun setup() = itemRepository.deleteAll()
            .also { orderRepository.deleteAll() }
            .let { (1..20) }
            .map { Item("Polozka $it", Random.nextDouble(1.0, 20.0)) }
            .also { itemRepository.saveAll(it) }
            .map { item ->
                (0..Random.nextInt(0, 5)).map {
                    Purchase(
                            item,
                            Random.nextInt(1, 21),
                            LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).minusWeeks(it.toLong()).toInstant().toEpochMilli()
                    )
                }
            }.flatten()
            .also { orderRepository.saveAll(it) }
            .let { itemRepository.findAll() }

    @RequestMapping("/all")
    fun getAll() = itemRepository.findAll()
}