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
import sk.pismaniacs.fiitcompany.model.Item
import sk.pismaniacs.fiitcompany.model.requests.*


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

    @RequestMapping("/delete", method = arrayOf(RequestMethod.POST), consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    @ResponseBody
    fun deleteItems(@RequestBody request: ModifyRequest): ModifyResponse {
        request.item_ids.forEach {
            itemRepository.deleteById(it.toLong())
        }
        return ModifyResponse("OK")
    }

    @RequestMapping("/save", method = arrayOf(RequestMethod.POST), consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    @ResponseBody
    fun saveItems(@RequestBody request: ModifyRequest2): ModifyResponse {
        itemRepository.saveAll(request.item_ids)
        return ModifyResponse("OK")
    }

    @RequestMapping("/allProducts")
    fun getAllProducts(model: Model): String {
        model.addAttribute("items", itemRepository.findAll().sortedBy { it.id })
        return "edit"
    }

    @RequestMapping("/editProducts2")
    fun editProducts(model: ModelMap, @RequestParam item_ids: Array<String>): String {
        model.addAttribute("items", itemRepository.findAllById(item_ids.map { it.toLong() }))
        return "edit"
    }

    @RequestMapping("/edit", method = arrayOf(RequestMethod.POST), consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun editTheProducts(model: Model, @RequestBody request: ModifyRequest): String {
        model.addAttribute("items", itemRepository.findAllById(request.item_ids.map { it.toLong() }))
        return "edit"
    }

    @RequestMapping("/addSeasonItem", method = arrayOf(RequestMethod.POST), consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun addSeasonItem(model: Model, @RequestBody request: ModifyRequest): String {
        itemRepository.findAllById(request.item_ids.map { it.toLong() }).map { it.copy(price = 0.9 * it.price, advertise = true) }.also { items ->
            itemRepository.saveAll(items)
            seasonRepository.findFirstByOrderByIdDesc().ifPresent {
                if (it.actual && it.editable) {
                    seasonRepository.save(it.copy(items = it.items + items))
                }
            }
        }

        seasonRepository.findFirstByOrderByIdDesc().ifPresent {
            if (it.editable) {
                model.addAttribute("seasonalItems", it.items.sortedBy { it.id })
                model.addAttribute("otherItems", (itemRepository.findAll() - it.items).sortedBy { it.id })
            } else {
                model.addAttribute("seasonalItems", it.items.sortedBy { it.id })
                model.addAttribute("otherItems", emptyList<Item>())
            }
        }


        return "actualSeason"
    }

    @RequestMapping("/removeSeasonItem", method = arrayOf(RequestMethod.POST), consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun removeSeasonItem(model: Model, @RequestBody request: ModifyRequest): String {
        itemRepository.findAllById(request.item_ids.map { it.toLong() }).map { it.copy(price = 1.1111 * it.price, advertise = false) }.also { items ->
            itemRepository.saveAll(items)
            seasonRepository.findFirstByOrderByIdDesc().ifPresent {
                if (it.actual && it.editable) {
                    seasonRepository.save(it.copy(items = it.items - items))
                }
            }
        }

        seasonRepository.findFirstByOrderByIdDesc().ifPresent {
            if (it.editable) {
                model.addAttribute("seasonalItems", it.items.sortedBy { it.id })
                model.addAttribute("otherItems", (itemRepository.findAll() - it.items).sortedBy { it.id })
            } else {
                model.addAttribute("seasonalItems", it.items.sortedBy { it.id })
                model.addAttribute("otherItems", emptyList<Item>())
            }
        }


        return "actualSeason"
    }

    @RequestMapping("/saveItem", method = arrayOf(RequestMethod.POST), consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun saveItem(model: Model, @RequestBody request: ModifyRequest3): String {
        val items = itemRepository.findAllById(listOf(request.item_ids.id.toLong()) )
        items.map { item ->
            item.copy(name = request.item_ids.name
                    ?: "", price = request.item_ids.price.toDouble() ?: 0.0)
        }.also {
            itemRepository.saveAll(it)
        }
        val requestId = request.report_id.first().toLong()
        val reports = notificationRepository.findAll()
                .filter { it.regularReports.isNotEmpty() }
                .map { it.regularReports }
                .filter {
                    it.map { it.id }.filterNotNull().contains(requestId)
                }.flatten()

        model.addAttribute("items", reports.sortedBy { it.id })
        return "showNotification"
    }


    @RequestMapping("/addAdvert", method = arrayOf(RequestMethod.POST), consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun addAdvert(model: Model, @RequestBody request: ModifyRequest4): String {
        val items = itemRepository.findAllById(request.item_ids.map { it.toLong() })
        items.map { item -> item.copy(advertise = true) }.also {
            itemRepository.saveAll(it)
        }

        val requestId = request.report_id.first().toLong()
        val reports = notificationRepository.findAll()
                .filter { it.regularReports.isNotEmpty() }
                .map { it.regularReports }
                .filter {
                    it.map { it.id }.filterNotNull().contains(requestId)
                }.flatten()

        model.addAttribute("items", reports.sortedBy { it.id })
        return "showNotification"
    }

    @RequestMapping("/actualSeason")
    fun getActualSeason(model: Model): String {
        seasonRepository.findFirstByOrderByIdDesc().ifPresent {
            if (it.editable) {
                model.addAttribute("seasonalItems", it.items.sortedBy { it.id })
                model.addAttribute("otherItems", (itemRepository.findAll() - it.items).sortedBy { it.id })
            } else {
                model.addAttribute("seasonalItems", it.items.sortedBy { it.id })
                model.addAttribute("otherItems", emptyList<Item>())
            }
        }
        return "actualSeason"
    }


    @RequestMapping("/notifications")
    fun getNotifications(model: Model): String {
        model.addAttribute("items", notificationRepository.findAll().sortedByDescending { it.id })
        return "notifications"
    }

    @RequestMapping("/notification", method = arrayOf(RequestMethod.POST), consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun showNotification(model: Model, @RequestBody request: ModifyRequest): String {
        notificationRepository
                .findAllById(request.item_ids.map { it.toLong() })
                .map { it.regularReports }
                .flatten()
                .filterNotNull()
                .also {
                    model.addAttribute("items", it)
                }
        return "showNotification"
    }

    @RequestMapping("/advert")
    fun showAdvert(model: Model): String {
        model.addAttribute("items", itemRepository.findAll().sortedBy { it.id }.filter { it.advertise })
        return "advert"
    }
}
