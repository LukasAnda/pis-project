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

@RestController
@RequestMapping("/items")
class ItemController {
    @Autowired
    lateinit var itemRepository: ItemRepository

    @Autowired
    lateinit var orderRepository: OrderRepository

    @Autowired
    lateinit var notificationRepository: NotificationRepository

    @RequestMapping("/all")
    fun getAll() = itemRepository.findAll().sortedBy { it.id }

    @RequestMapping("/notifications")
    fun getNotifications() = notificationRepository.findAll()

}