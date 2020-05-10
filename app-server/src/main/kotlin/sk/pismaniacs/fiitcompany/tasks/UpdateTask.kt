package sk.pismaniacs.fiitcompany.tasks

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import sk.pismaniacs.fiitcompany.model.*
import sk.pismaniacs.fiitcompany.repository.*
import sk.stuba.fiit.predmety.pis.pis.notificationservices.email.EmailService
import sk.stuba.fiit.predmety.pis.pis.notificationservices.email.types.Notify
import sk.stuba.fiit.predmety.pis.pis.notificationservices.mail.MailService
import sk.stuba.fiit.predmety.pis.pis.notificationservices.sms.SMSService
import sk.stuba.fiit.predmety.pis.pis.students.team022kontrolastavu.Team022KontrolaStavuService
import sk.stuba.fiit.predmety.pis.pis.students.team022kontrolastavu.types.Insert
import sk.stuba.fiit.predmety.pis.pis.students.team022kontrolastavu.types.KontrolaStavu2
import sk.stuba.fiit.predmety.pis.pis.students.team022objednavka.Team022ObjednavkaService
import sk.stuba.fiit.predmety.pis.pis.students.team022objednavka.types.Objednavka
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import javax.annotation.PostConstruct
import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.XMLGregorianCalendar
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

    private val emailPort = EmailService().emailPort
    private var mailPort = MailService().mailPort
    private var smsPort = SMSService().smsPort
    private var kontrolaStavuPort = Team022KontrolaStavuService().team022KontrolaStavuPort
    private var objednavkaPort = Team022ObjednavkaService().team022ObjednavkaPort


    private var counter = 0

    @PostConstruct
    fun setup() {
        itemRepository.deleteAll()
                .also { orderRepository.deleteAll() }
                .let { (1..50) }
                .map { Item("Polozka $it", Random.nextDouble(1.0, 20.0)) }
                .also { itemRepository.saveAll(it) }
                .let { itemRepository.findAll() }

        generateNextWeekTransactions()
        generateNextWeekTransactions()

    }

    infix fun Int.divisible(other: Int) = this % other == 0

    @Scheduled(fixedRate = 1000 * 60)
    fun update() {
        println("Update tick")

        if (counter divisible 48) {
            println("New season")
            nextSeason()
        }

        if ((counter - 1) divisible 48) {
            println("Lock current season")
            lockActualSeason()
        }

        if (counter divisible 3) {
            println("Update prices")
            updatePricesTask()
        }

        counter++
    }

    fun lockActualSeason() {
        seasonRepository.findFirstByOrderByIdDesc().ifPresent {
            seasonRepository.save(it.copy(editable = false))
        }
    }

    private fun nextSeason() {
        seasonRepository.findFirstByOrderByIdDesc().ifPresent {
            seasonRepository.save(it.copy(actual = false))
            it.items.map { it.copy(price = 1.1111 * it.price) }.let {
                itemRepository.saveAll(it)
            }
        }

        sendInfoToClients()
        check()

        val lastSeason = seasonRepository.findFirstByOrderByIdDesc().orElseGet { Season() }.id ?: 0
        itemRepository.findAll().shuffled().take(10).let {

            it.map { it.copy(price = 0.9 * it.price) }.let {
                itemRepository.saveAll(it)
            }

            Season("Season ${lastSeason + 1}", System.currentTimeMillis(), true, true, it)
        }.also {
            seasonRepository.save(it)
        }.also {
            val seasonalReports = it.items.map { SeasonalReport(it) }
            seasonalReportRepository.saveAll(seasonalReports)
            notificationRepository.save(Notification("New season: ${it.name}", true, seasonalReports = seasonalReports))
        }

    }

    private fun updatePricesTask() {
        notificationRepository
                .findAll()
                .filter { (it.regularReports.isNotEmpty() || it.seasonalPriceReports.isNotEmpty()) && it.actual }
                .map { it.copy(actual = false) }
                .also { notificationRepository.saveAll(it) }
                .filter { it.regularReports.isNotEmpty() }
                .firstOrNull()
                ?.let { it.regularReports }
                ?.map { it.item }
                ?.filterNotNull()
                ?.map { it.copy(price = 0.9 * it.price) }
                ?.also {itemRepository.saveAll(it)}

        generateNextWeekTransactions()

        val seasonalItems = seasonRepository.findFirstByOrderByIdDesc().orElseGet { Season() }.items.map { it.id }

        orderRepository.findAll()
                .filter { !seasonalItems.contains(it.item?.id) }
                .groupBy { it.item }
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
                .filter { seasonalItems.contains(it.item?.id) }
                .groupBy { it.item }
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

    private fun sendInfoToClients(){
        val emailResponse = emailPort.notify(Notify().apply {
            email = "lukas.anda@gmail.com"
            subject = "New Season"
            message = "Hey, new season arrived"
            teamId = "022"
            password = "Y6ZSLR"
        })
        if(emailResponse.isSuccess){
            println("Email sent!!!")
        } else {
            println("Email failed!!!")
        }
        val mailResponse = mailPort.notify(sk.stuba.fiit.predmety.pis.pis.notificationservices.mail.types.Notify().apply {
            address = "Ilkovicova 2"
            subject = "New Season"
            message = "Hey, new season arrived"
            teamId = "022"
            password = "Y6ZSLR"
        })
        if(mailResponse.isSuccess){
            println("Mail sent!!!")
        } else {
            println("Mail failed!!!")
        }
        val smsResponse = smsPort.notify(sk.stuba.fiit.predmety.pis.pis.notificationservices.sms.types.Notify().apply {
            phone = "0918999000"
            subject = "New Season"
            message = "Hey, new season arrived"
            teamId = "022"
            password = "Y6ZSLR"
        })

        if(smsResponse.isSuccess){
            println("Sms sent!!!")
        } else {
            println("Sms failed!!!")
        }
    }

    private fun check(){
        val response = kontrolaStavuPort.insert(Insert().apply {
            teamId = "022"
            teamPassword = "Y6ZSLR"
            kontrolaStavu = KontrolaStavu2().apply {
                name = "Nova kontrola stavu"
                datum = DatatypeFactory.newInstance().newXMLGregorianCalendar()
            }
        })

        println("New check has id: ${response.id}")
    }

    private fun requestItem(item: Item){
        val response = objednavkaPort.insert(sk.stuba.fiit.predmety.pis.pis.students.team022objednavka.types.Insert().apply {
            teamId = "022"
            teamPassword = "Y6ZSLR"
            objednavka = Objednavka().apply {
                name = item.name
                pocet = (item.purchases.sortedByDescending { it.dateOfPurchase }.firstOrNull()?.quantity ?: 0) / 2
            }
        })

        println("New request has id: ${response.id}")
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
        priceChange > 10 -> {
            //item?.let { requestItem(it) }
            Pair(item?.copy(price = item.price * 0.9), priceChange)
        }
        else -> Pair(item, priceChange)
    }

    private fun percentageDifference(value1: Int, value2: Int) = 100.0 * ((value1.toDouble() - value2.toDouble()) / ((value1.toDouble() + value2.toDouble()) / 2))

}