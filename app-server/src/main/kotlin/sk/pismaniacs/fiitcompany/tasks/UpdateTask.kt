package sk.pismaniacs.fiitcompany.tasks

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import sk.pismaniacs.fiitcompany.model.Item
import sk.pismaniacs.fiitcompany.model.Notification
import sk.pismaniacs.fiitcompany.model.Purchase
import sk.pismaniacs.fiitcompany.repository.ItemRepository
import sk.pismaniacs.fiitcompany.repository.NotificationRepository
import sk.pismaniacs.fiitcompany.repository.OrderRepository
import sk.pismaniacs.fiitcompany.tasks.UpdateTask.PriceUpdate.*
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import javax.annotation.PostConstruct
import kotlin.random.Random

@Component
class UpdateTask {
    @Autowired
    lateinit var itemRepository: ItemRepository

    @Autowired
    lateinit var orderRepository: OrderRepository

    @Autowired
    lateinit var notificationRepository: NotificationRepository

    @PostConstruct
    fun setup() {
        itemRepository.deleteAll()
                .also { orderRepository.deleteAll() }
                .let { (1..100) }
                .map { Item("Polozka $it", Random.nextDouble(1.0, 20.0)) }
                .also { itemRepository.saveAll(it) }
                .let { itemRepository.findAll() }

        generateNextWeekTransactions()
        generateNextWeekTransactions()

    }

    @Scheduled(fixedRate = 1000 * 60)
    fun updatePricesTask() {

        notificationRepository.findAll().map { it.item }.filterNotNull().map { it.copy(price = it.price * 0.8) }.also {
            itemRepository.saveAll(it)
        }

        notificationRepository.deleteAll()

        generateNextWeekTransactions()

        orderRepository.findAll()
                .groupBy { it.item }
                .map { mapItem ->
                    // Transform into changed item
                    mapItem.value // Get list of purchases for selected item
                            .sortedByDescending { it.dateOfPurchase } // Sort them by date in case the flatMap mismatched them
                            .take(2)
                            .let { Pair(it.first(), it.last()) } // Make a pair
                            .let { percentageDifference(it.first.quantity, it.second.quantity) } // Get difference
                            .let {
                                when {
                                    it > 10 -> INCREASE
                                    it < -20 -> DECREASE
                                    else -> STAY
                                }
                            }
                            .let { updateItem(mapItem.key, it) }

                }
                .also {
                    it.map { it.first }.also {
                        itemRepository.saveAll(it)
                    }
                }
                .also {
                    it.filter { it.second == DECREASE }.map { Notification(it.first) }.also {
                        notificationRepository.saveAll(it)
                    }
                }


    }

    private fun generateNextWeekTransactions() = orderRepository.findAll()
            .sortedByDescending { it.dateOfPurchase }
            .let {
                Instant.ofEpochMilli(it.firstOrNull()?.dateOfPurchase
                        ?: System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toLocalDate()
            }.let { date ->
                val items = itemRepository.findAll().toList()

                items.map {
                    Purchase(
                            it,
                            Random.nextInt(1, 100),
                            date.plusWeeks(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                    )
                }
            }.let {
                orderRepository.saveAll(it)
            }


    enum class PriceUpdate {
        INCREASE, DECREASE, STAY
    }

    private fun updateItem(item: Item?, updateType: PriceUpdate) = when (updateType) {
        INCREASE -> Pair(item?.copy(price = item.price * 0.9), updateType)
        else -> Pair(item, updateType)
    }

    private fun percentageDifference(value1: Int, value2: Int) = 100.0 * ((value1.toDouble() - value2.toDouble()) / ((value1.toDouble() + value2.toDouble()) / 2))

}