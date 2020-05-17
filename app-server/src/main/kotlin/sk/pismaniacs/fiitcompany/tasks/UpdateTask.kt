package sk.pismaniacs.fiitcompany.tasks

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import sk.pismaniacs.fiitcompany.model.*
import sk.pismaniacs.fiitcompany.repository.*
import sk.pismaniacs.fiitcompany.wsdl.EmailRepository
import sk.pismaniacs.fiitcompany.wsdl.MailRepository
import sk.pismaniacs.fiitcompany.wsdl.ProductsRepository
import sk.pismaniacs.fiitcompany.wsdl.SmsRepository
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import javax.annotation.PostConstruct
import kotlin.random.Random
import sk.pismaniacs.fiitcompany.wsdl.OrderRepository as WsdlOrderRepository


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


    private var counter = 0

    // This gets called after startup of the app
    @PostConstruct
    fun setup() {

        itemRepository.deleteAll() // Remove all items
                .also { orderRepository.deleteAll() } // Remove all orders
                .let { (1..50) } // Create interval of 50 items
                .map { Item("Polozka $it", Random.nextDouble(1.0, 20.0)) } // Create sample items with random price
                .also { itemRepository.saveAll(it) } // Save items

        generateNextWeekTransactions() // Generate transactions twice so they will be ready for testing
        generateNextWeekTransactions()

        // Add items to PIS database on webpage using WSDL
        ProductsRepository.insertProducts(itemRepository.findAll().toList())

    }

    // Just a helper function to find if one number is divisible by other
    infix fun Int.divisible(other: Int) = this % other == 0

    // Periodic task scheduled at fixed rate of 1 minute
    @Scheduled(fixedRate = 1000 * 60)
    fun update() {
        println("Update tick")

        // If 3 months have passed
        if (counter divisible 48) {
            println("New season")
            nextSeason()
        }

        // If 3 days after season started have passed
        if ((counter - 1) divisible 48) {
            println("Lock current season")
            lockActualSeason()
        }

        // If one week has passed
        if (counter divisible 3) {
            println("Update prices")
            updatePricesTask()
            updateProductPrices()
        }

        counter++
    }

    // Marks the current season as non-editable
    fun lockActualSeason() {
        seasonRepository.findFirstByOrderByIdDesc().ifPresent {
            seasonRepository.save(it.copy(editable = false))
        }
    }

    // Create next season
    private fun nextSeason() {
        // Get actual season
        seasonRepository.findFirstByOrderByIdDesc().ifPresent {
            // Mark it as not actual
            seasonRepository.save(it.copy(actual = false))
            // Remove the items mentioned in that season from advertisement and update their prices
            it.items.map { it.copy(price = 1.1111 * it.price, advertise = false) }.let {
                // Save items
                itemRepository.saveAll(it)
            }
        }

        // Get last season id to determine name
        val lastSeason = seasonRepository.findFirstByOrderByIdDesc().orElseGet { Season() }.id ?: 0

        itemRepository // Get item repository
                .findAll() // Find all items
                .filter { it.isSelling } // Get only those that are selling
                .shuffled() // Shuffle them
                .take(10) // Take 10 first items
                .let {

                    // Get ids of products that have 0 quantity
                    ProductsRepository.getIdsToOrder().intersect(it.map { it.name }).forEach {
                        //Order them
                        WsdlOrderRepository.orderItem(it, 100)
                    }

                    // Update price to be 90% and put the items in advertising
                    it.map { it.copy(price = 0.9 * it.price, advertise = true) }
                            .let { itemRepository.saveAll(it) }

                    // Create new season
                    Season("Season ${lastSeason + 1}", System.currentTimeMillis(), true, true, it)
                }.also {
                    // Save new season
                    seasonRepository.save(it)
                }.also {
                    // Create season reports from items
                    val seasonalReports = it.items.map { SeasonalReport(it) }
                    // Save them to db
                    seasonalReportRepository.saveAll(seasonalReports)
                    // Create seasonal notification
                    notificationRepository.save(Notification("New season: ${it.name}", true, seasonalReports = seasonalReports))
                }

        // Send info to clients about new season
        sendInfoToClients()

    }

    // Updates prices once a week
    private fun updatePricesTask() {
        notificationRepository // Get notification repository
                .findAll() // Find all
                .filter { (it.regularReports.isNotEmpty() || it.seasonalPriceReports.isNotEmpty()) && it.actual } // Filter those with seasonal and regular price reports
                .map { it.copy(actual = false) } // Make them not actual
                .also { notificationRepository.saveAll(it) } // Save them
                .filter { it.regularReports.isNotEmpty() } // Filter only with regular reports
                .firstOrNull() // Get most recent or null
                ?.let { it.regularReports } // Get only reports
                ?.map { it.item } // Get only items
                ?.filterNotNull() // Filter not null
                ?.map { it.copy(price = 0.9 * it.price) } // Set their price to be 90%
                ?.also { itemRepository.saveAll(it) } // Save them

        // Get next week transactions
        generateNextWeekTransactions()

        // Get seasonal items to compare against
        val seasonalItems = seasonRepository.findFirstByOrderByIdDesc().orElseGet { Season() }.items.map { it.id }


        orderRepository.findAll() // Find all orders
                .filter { !seasonalItems.contains(it.item?.id) } // Filter only non seasonal
                .groupBy { it.item } // Group them by item
                .map { mapItem ->
                    mapItem.value // Get list of purchases for selected item
                            .sortedByDescending { it.dateOfPurchase } // Sort them by date in case the flatMap mismatched them
                            .drop(2)
                            .let {
                                orderRepository.deleteAll(it)
                            }

                    // Transform into changed item
                    mapItem.value // Get list of purchases for selected item
                            .sortedByDescending { it.dateOfPurchase } // Sort them by date in case the flatMap mismatched them
                            .take(2)
                            .let { Pair(it.first(), it.last()) } // Make a pair
                            .let { percentageDifference(it.first.quantity, it.second.quantity) } // Get difference
                            .let { updateItem(mapItem.key, it) } // Update item


                }
                .also {
                    it.map { it.first }.also {
                        itemRepository.saveAll(it) // Save updated items
                    }
                }
                .also {
                    it.filter { it.second <= -20 } // Filter those with difference larger than -20%
                            .map { RegularReport(it.first, it.second) } // Map them to regular reports
                            .also {
                                if (it.isNotEmpty()) {
                                    regularReportRepository.saveAll(it) // Save them
                                    val newNotification = Notification("New price update", true, it) // Create new price notification
                                    notificationRepository.save(newNotification) // Save notification
                                }
                            }
                }


        orderRepository.findAll() // Find all orders
                .filter { seasonalItems.contains(it.item?.id) } // Filter only seasonal
                .groupBy { it.item } // Group them by item
                .map { mapItem ->
                    mapItem.value // Get list of purchases for selected item
                            .sortedByDescending { it.dateOfPurchase } // Sort them by date in case the flatMap mismatched them
                            .drop(2)
                            .let {
                                orderRepository.deleteAll(it)
                            }
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
                        itemRepository.saveAll(it) // Save updated items
                    }
                }
                .also {
                    it.filter { it.second <= -20 } // Filter those with difference larger than -20%
                            .map {
                                Pair(it.first?.copy(price = (it.first?.price ?: 0.0) / 2), it.second)
                            } // Update their price to be exactly 50%
                            .also {
                                it.map { it.first }.filterNotNull().also {
                                    itemRepository.saveAll(it) // Save items
                                }
                            }
                            .map { SeasonalPriceReport(it.first, it.second) } // Map to SeasonalPriceReports
                            .also {
                                if (it.isNotEmpty()) {
                                    seasonalPriceReportRepository.saveAll(it) // Save reports
                                    val newNotification = Notification("New seasonal price update", true, seasonalPriceReports = it) // Create notification
                                    notificationRepository.save(newNotification) // Save notification
                                }
                            }
                }
    }

    // Sand info using wsdl
    private fun sendInfoToClients() {
        EmailRepository.sendEmail()
        MailRepository.sendMail()
        SmsRepository.sendSms()
    }

    // Update product quantities using wsdl
    private fun updateProductPrices() {
        ProductsRepository.updateProductQuantities()
    }

    // Request specific item
    private fun requestItem(item: Item) {
        WsdlOrderRepository.orderItem(item.name, (item.purchases
                .sortedByDescending { it.dateOfPurchase }
                .firstOrNull()
                ?.quantity ?: 0)
                / 2)
    }

    // Create transactions for next week
    private fun generateNextWeekTransactions() = orderRepository.findAll() // Get all orders
            .sortedByDescending { it.dateOfPurchase } // Sort them descending by date of purchase
            .let { Instant.ofEpochMilli(it.firstOrNull()?.dateOfPurchase  ?: System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toLocalDate() } // Get it as date
            .let { date ->
                val items = itemRepository.findAll().toList().filter { it.isSelling } // Get selling items

                items.map {
                    // Create new purchase
                    Purchase(
                            it,
                            Random.nextInt(1, 100),
                            date.plusWeeks(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                    )
                }
            }.let {
                // Save purchases
                orderRepository.saveAll(it)
            }

    // Update item if purchaseChange has been greater than 10%
    private fun updateItem(item: Item?, purchaseChange: Double) = when {
        purchaseChange > 10 -> {
            item?.let { requestItem(it) }
            Pair(item?.copy(price = item.price * 0.9), purchaseChange)
        }
        else -> Pair(item, purchaseChange)
    }

    // Calculate difference between two purchases in percent
    private fun percentageDifference(value1: Int, value2: Int) = 100.0 * ((value1.toDouble() - value2.toDouble()) / ((value1.toDouble() + value2.toDouble()) / 2))

}