package sk.pismaniacs.fiitcompany.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.http.MediaType
import sk.pismaniacs.fiitcompany.repository.ItemRepository
import sk.pismaniacs.fiitcompany.repository.NotificationRepository
import sk.pismaniacs.fiitcompany.repository.OrderRepository
import sk.pismaniacs.fiitcompany.repository.SeasonRepository
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import sk.pismaniacs.fiitcompany.model.requests.DeleteRequest
import sk.pismaniacs.fiitcompany.model.requests.DeleteResponse


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
        model.addAttribute("mode", appMode)
        model.addAttribute("items", itemRepository.findAll().sortedBy { it.id })
        return "index"
    }

    @RequestMapping("/delete" , method = arrayOf(RequestMethod.POST), consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    @ResponseBody
    fun checkUsername(@RequestBody request: DeleteRequest): DeleteResponse {
        request.item_ids.forEach {
            itemRepository.deleteById(it.toLong())
        }
        return DeleteResponse("PISko je picovina")
    }

    @RequestMapping("/allProducts")
    fun getAllProducts(model: Model): String{
        model.addAttribute("items", itemRepository.findAll().sortedBy { it.id })
        return "edit"
    }
}
