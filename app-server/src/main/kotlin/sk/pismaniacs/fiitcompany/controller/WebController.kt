package sk.pismaniacs.fiitcompany.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import java.util.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import sk.pismaniacs.fiitcompany.repository.ItemRepository
import sk.pismaniacs.fiitcompany.repository.NotificationRepository
import sk.pismaniacs.fiitcompany.repository.OrderRepository
import sk.pismaniacs.fiitcompany.repository.SeasonRepository


@Controller
class WebController {
    lateinit var appMode: String
    @Autowired
    fun WebAppContoller(environment: Environment) {
        appMode = environment.getProperty("app-mode") ?: ""
    }

    @Autowired
    lateinit var itemRepository: ItemRepository

    @Autowired
    lateinit var orderRepository: OrderRepository

    @Autowired
    lateinit var notificationRepository: NotificationRepository

    @Autowired
    lateinit var seasonRepository: SeasonRepository


    @RequestMapping("/")
    fun index(model: Model): String {
        model.addAttribute("datetime", Date())
        model.addAttribute("username", "Peder")
        model.addAttribute("mode", appMode)
        return "index"
    }

    @RequestMapping("/allProducts")
    fun getAllProducts(model: Model): String{
        model.addAttribute("items", itemRepository.findAll().sortedBy { it.id })
        return "TuSiDajNazovStranky"
    }
}