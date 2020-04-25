package sk.pismaniacs.fiitcompany.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import sk.pismaniacs.fiitcompany.model.Item
import sk.pismaniacs.fiitcompany.model.Purchase
import sk.pismaniacs.fiitcompany.repository.ItemRepository
import sk.pismaniacs.fiitcompany.repository.NotificationRepository
import sk.pismaniacs.fiitcompany.repository.OrderRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.random.Random
import sun.security.x509.OIDMap.addAttribute
import org.springframework.ui.Model
import sk.pismaniacs.fiitcompany.repository.SeasonRepository
import java.util.*


@RestController
class ItemController {
    @Autowired
    lateinit var itemRepository: ItemRepository

    @Autowired
    lateinit var orderRepository: OrderRepository

    @Autowired
    lateinit var notificationRepository: NotificationRepository

    @Autowired
    lateinit var seasonRepository: SeasonRepository

    @RequestMapping("/all")
    fun getAll() = itemRepository.findAll().sortedBy { it.id }

    @RequestMapping("/notifications")
    fun getNotifications() = notificationRepository.findAll()

    @RequestMapping("/seasons")
    fun getSeasons() = seasonRepository.findAll()

    @RequestMapping("/addSeason")
    fun addSeason() = itemRepository.findAll()

    @RequestMapping("/")
    fun index(model: Model): String {
        model.addAttribute("datetime", Date())
        model.addAttribute("username", "Kokot")

        return "index"
    }

}