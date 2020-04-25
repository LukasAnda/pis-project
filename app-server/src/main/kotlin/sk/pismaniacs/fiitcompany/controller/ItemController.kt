package sk.pismaniacs.fiitcompany.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import sk.pismaniacs.fiitcompany.repository.ItemRepository
import sk.pismaniacs.fiitcompany.repository.NotificationRepository
import sk.pismaniacs.fiitcompany.repository.OrderRepository
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.PathVariable
import sk.pismaniacs.fiitcompany.model.Season
import sk.pismaniacs.fiitcompany.repository.SeasonRepository
import java.util.*


@RestController
@RequestMapping("/json")
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

    @RequestMapping("/addSeason/{seasonName}")
    fun addSeason(@PathVariable seasonName: String) = itemRepository.findAll() // Get all items
            .shuffled()// Shuffle them
            .take(4)// Get 4 first (guaranteed to be unique and random due to shuffle
            .let {
                // Map to Season
                Season(seasonName, System.currentTimeMillis(), it)
            }.let {
                // Insert the season to DB
                seasonRepository.save(it)
            }.let {
                // Return all seasons
                seasonRepository.findAll()
            }

}