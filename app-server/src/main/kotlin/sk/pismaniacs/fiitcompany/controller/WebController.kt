package sk.pismaniacs.fiitcompany.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.http.MediaType
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.*
import sk.pismaniacs.fiitcompany.repository.ItemRepository
import sk.pismaniacs.fiitcompany.repository.NotificationRepository
import sk.pismaniacs.fiitcompany.repository.OrderRepository
import sk.pismaniacs.fiitcompany.repository.SeasonRepository
import org.springframework.web.servlet.ModelAndView
import sk.pismaniacs.fiitcompany.model.requests.ModifyRequest
import sk.pismaniacs.fiitcompany.model.requests.ModifyRequest2
import sk.pismaniacs.fiitcompany.model.requests.ModifyResponse


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
    fun deleteItems(@RequestBody request: ModifyRequest): ModifyResponse {
        request.item_ids.forEach {
            itemRepository.deleteById(it.toLong())
        }
        return ModifyResponse("OK")
    }

    @RequestMapping("/save" , method = arrayOf(RequestMethod.POST), consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    @ResponseBody
    fun saveItems(@RequestBody request: ModifyRequest2): ModifyResponse {
        itemRepository.saveAll(request.item_ids)
        return ModifyResponse("OK")
    }

    @RequestMapping("/allProducts")
    fun getAllProducts(model: Model): String{
        model.addAttribute("items", itemRepository.findAll().sortedBy { it.id })
        return "edit"
    }

    @RequestMapping("/editProducts2")
    fun editProducts(model: ModelMap, @RequestParam item_ids: Array<String>): String{
        model.addAttribute("items", itemRepository.findAllById(item_ids.map { it.toLong() }))
        return "edit"
    }

    @RequestMapping("/edit" , method = arrayOf(RequestMethod.POST), consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun editTheProducts(model: Model, @RequestBody request: ModifyRequest): String {
        model.addAttribute("items", itemRepository.findAllById(request.item_ids.map { it.toLong() }))
        return "edit"
    }

    @RequestMapping("/actualSeason")
    fun getActualSeason(model: Model): String{
        model.addAttribute("items", seasonRepository.findFirstByOrderByIdDesc().get().items)
        model.addAttribute("season", seasonRepository.findFirstByOrderByIdDesc().get())
        return "actualSeason"
    }

    @RequestMapping("/notifications")
    fun getNotifications(model: Model): String{
        model.addAttribute("items", notificationRepository.findAll().sortedByDescending { it.id })
        return "notifications"
    }
}
