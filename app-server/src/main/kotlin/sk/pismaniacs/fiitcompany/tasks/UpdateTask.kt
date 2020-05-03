package sk.pismaniacs.fiitcompany.tasks

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import sk.pismaniacs.fiitcompany.model.*
import sk.pismaniacs.fiitcompany.repository.*
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
    lateinit var seasonRepository: SeasonRepository

    @Autowired
    lateinit var notificationRepository: NotificationRepository

    @Autowired
    lateinit var regularReportRepository: RegularReportRepository

    @Autowired
    lateinit var seasonalReportRepository: SeasonalReportsRepository

    @Autowired
    lateinit var seasonalPriceReportRepository: SeasonalPriceReportRepository

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

    @Scheduled(fixedRate = 1000 * 60 * 48)
    fun nextSeason() {
        System.out.println("Next season")
        seasonRepository.findFirstByOrderByIdDesc().ifPresent {
            seasonRepository.save(it.copy(actual = false))
        }

        val lastSeason = seasonRepository.findFirstByOrderByIdDesc().orElseGet { Season() }.id ?: 0
        itemRepository.findAll().shuffled().take(10).let {
            Season("Season ${lastSeason + 1}", System.currentTimeMillis(), true, it)
        }.also {
            seasonRepository.save(it)
        }.also {
            val seasonalReports = it.items.map { SeasonalReport(it) }
            seasonalReportRepository.saveAll(seasonalReports)
            notificationRepository.save(Notification("New season: ${it.name}", true, seasonalReports = seasonalReports))
        }
    }

    @Scheduled(fixedRate = 1000 * 60 * 3)
    fun updatePricesTask() {
        System.out.println("Update price")
        notificationRepository
                .findAll()
                .filter { (it.regularReports.isNotEmpty() || it.seasonalPriceReports.isNotEmpty()) && it.actual }
                .map { it.copy(actual = false) }
                .also {
                    notificationRepository.saveAll(it)
                }

        generateNextWeekTransactions()

        val seasonalItems = seasonRepository.findFirstByOrderByIdDesc().orElseGet { Season() }.items

        orderRepository.findAll()
                .filter { !seasonalItems.contains(it.item) }
                .groupBy { it.item }
                .map { mapItem ->
                    // Transform into changed item
                    mapItem.value // Get list of purchases for selected item
                            .sortedByDescending { it.dateOfPurchase } // Sort them by date in case the flatMap mismatched them
                            .take(2)
                            .let { Pair(it.first(), it.last()) } // Make a pair
                            .let { percentageDifference(it.first.quantity, it.second.quantity) } // Get difference
                            .let { updateItem(mapItem.key, it) }

                }
                .also {
                    it.map { it.first }.also {
                        itemRepository.saveAll(it)
                    }
                }
                .also {
                    it.filter { it.second <= -20 }.map { RegularReport(it.first, it.second) }.also {
                        if (it.isNotEmpty()) {
                            regularReportRepository.saveAll(it)
                            val newNotification = Notification("New price update", true, it)
                            notificationRepository.save(newNotification)
                        }
                    }
                }

        orderRepository.findAll()
                .filter { seasonalItems.contains(it.item) }
                .groupBy { it.item }
                .map { mapItem ->
                    // Transform into changed item
                    mapItem.value // Get list of purchases for selected item
                            .sortedByDescending { it.dateOfPurchase } // Sort them by date in case the flatMap mismatched them
                            .take(2)
                            .let { Pair(it.first(), it.last()) } // Make a pair
                            .let { percentageDifference(it.first.quantity, it.second.quantity) } // Get difference
                            .let { Pair(mapItem.key, it) }

                }
                .also {
                    it.map { it.first }.also {
                        itemRepository.saveAll(it)
                    }
                }
                .also {
                    it.filter { it.second <= -20 }.map { SeasonalPriceReport(it.first, it.second) }.also {
                        if (it.isNotEmpty()) {
                            seasonalPriceReportRepository.saveAll(it)
                            val newNotification = Notification("New seasonal price update", true, seasonalPriceReports = it)
                            notificationRepository.save(newNotification)
                        }
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

    private fun updateItem(item: Item?, priceChange: Double) = when {
        priceChange > 10 -> Pair(item?.copy(price = item.price * 0.9), priceChange)
        else -> Pair(item, priceChange)
    }

    private fun percentageDifference(value1: Int, value2: Int) = 100.0 * ((value1.toDouble() - value2.toDouble()) / ((value1.toDouble() + value2.toDouble()) / 2))

}