package sk.pismaniacs.fiitcompany.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.*
import sk.pismaniacs.fiitcompany.model.Item
import sk.pismaniacs.fiitcompany.model.requests.*
import sk.pismaniacs.fiitcompany.repository.*


@Controller
class WebController {

    @Autowired
    lateinit var itemRepository: ItemRepository

    @Autowired
    lateinit var notificationRepository: NotificationRepository

    @Autowired
    lateinit var regularReportRepository: RegularReportRepository

    @Autowired
    lateinit var seasonRepository: SeasonRepository


    //Entrypoint to the webpage
    @RequestMapping("/")
    fun index(model: Model): String {
        // Add products to attribute named items
        model.addAttribute("items", itemRepository
                .findAll()
                .filter { it.isSelling }
                .sortedBy { it.id })
        // Return page index.html
        return "index"
    }

    // Remove items from selling from index.html
    @RequestMapping("/delete", method = arrayOf(RequestMethod.POST), consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    @ResponseBody
    fun deleteItems(@RequestBody request: ModifyRequest): ModifyResponse {
        itemRepository // Get item repository
                .findAllById(request.item_ids.map { it.toLong() }) // Find only those metioned in request by id
                .map { it.copy(isSelling = false) } // Edit them to be not selling
                .let { itemRepository.saveAll(it) } // Save to db
        // Return 200
        return ModifyResponse("OK")
    }

    // Get actual season
    @RequestMapping("/actualSeason")
    fun getActualSeason(model: Model): String {
        // Get actual season
        seasonRepository.findFirstByOrderByIdDesc().ifPresent {
            // If the season is editable
            if (it.editable) {
                model.addAttribute("seasonalItems", it.items.sortedBy { it.id }) // Add items from this season
                model.addAttribute("otherItems", (itemRepository.findAll().filter { it.isSelling } - it.items).sortedBy { it.id }) // Get all items and remove seasonal
            } else {
                // If it is not editable
                model.addAttribute("seasonalItems", it.items.sortedBy { it.id }) // Add items from this season
                model.addAttribute("otherItems", emptyList<Item>()) // Prevent adding items by not providing other items
            }
            // Tell if the season is editable
            model.addAttribute("isEditable", it.editable)
        }

        // Return actualSeson.html
        return "actualSeason"
    }

    // Add item to season
    @RequestMapping("/addSeasonItem", method = arrayOf(RequestMethod.POST), consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun addSeasonItem(model: Model, @RequestBody request: ModifyRequest): String {
        // Get actual season
        seasonRepository.findFirstByOrderByIdDesc().ifPresent {
            // If the season is editable
            if (it.editable) {
                itemRepository // Get item repository
                        .findAllById(request.item_ids.map { it.toLong() }) // Find all items mentioned in request
                        .map { it.copy(price = 0.9 * it.price, advertise = true) } // Change their price to be 90%
                        .also { items ->
                            seasonRepository.findFirstByOrderByIdDesc().ifPresent {
                                // If season is editable and is actual (might have expired)
                                if (it.actual && it.editable) {
                                    //Save item to season
                                    itemRepository.saveAll(items) // Save them
                                    seasonRepository.save(it.copy(items = it.items + items))
                                }
                            }
                        }

                model.addAttribute("seasonalItems", it.items.sortedBy { it.id }) // Add items from this season
                model.addAttribute("otherItems", (itemRepository.findAll().filter { it.isSelling } - it.items).sortedBy { it.id }) // Get all items and remove seasonal
            } else {
                // If it is not editable
                model.addAttribute("seasonalItems", it.items.sortedBy { it.id }) // Add items from this season
                model.addAttribute("otherItems", emptyList<Item>()) // Prevent adding items by not providing other items
            }
            // Tell if the season is editable
            model.addAttribute("isEditable", it.editable)
        }

        // Return actualSeson.html
        return "actualSeason"
    }

    @RequestMapping("/removeSeasonItem", method = arrayOf(RequestMethod.POST), consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun removeSeasonItem(model: Model, @RequestBody request: ModifyRequest): String {

        // Get actual season
        seasonRepository.findFirstByOrderByIdDesc().ifPresent {
            // If the season is editable
            if (it.editable) {
                itemRepository // Get item repository
                        .findAllById(request.item_ids.map { it.toLong() }) // Find all items mentioned in request
                        .map { it.copy(price = 1.1111 * it.price, advertise = true) } // Change their price to be 111% (restore to previous price)
                        .also { items ->
                            seasonRepository.findFirstByOrderByIdDesc().ifPresent {
                                // If season is editable and is actual (might have expired)
                                if (it.actual && it.editable) {
                                    //Save item to season
                                    itemRepository.saveAll(items) // Save them
                                    seasonRepository.save(it.copy(items = it.items - items))
                                }
                            }
                        }

                model.addAttribute("seasonalItems", it.items.sortedBy { it.id }) // Add items from this season
                model.addAttribute("otherItems", (itemRepository.findAll().filter { it.isSelling } - it.items).sortedBy { it.id }) // Get all items and remove seasonal
            } else {
                // If it is not editable
                model.addAttribute("seasonalItems", it.items.sortedBy { it.id }) // Add items from this season
                model.addAttribute("otherItems", emptyList<Item>()) // Prevent adding items by not providing other items
            }
            // Tell if the season is editable
            model.addAttribute("isEditable", it.editable)
        }

        // Return actualSeson.html
        return "actualSeason"
    }

    // Show all notifications
    @RequestMapping("/notifications")
    fun getNotifications(model: Model): String {
        model.addAttribute("items", notificationRepository.findAll().sortedByDescending { it.id })
        return "notifications"
    }

    // Show specific notification
    @RequestMapping("/notification", method = arrayOf(RequestMethod.POST), consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun showNotification(model: Model, @RequestBody request: ModifyRequest): String {
        // get notification repository
        notificationRepository
                .findAllById(request.item_ids.map { it.toLong() }) // Find all requested by id
                .map { it.regularReports } // Get reports
                .flatten() // Flatten if request contained multiple ids
                .also { model.addAttribute("items", it) } // Add them to attribute "items"

        // Return showNotification.html
        return "showNotification"
    }

    // Save item modified in showNotification.html
    @RequestMapping("/saveItem", method = arrayOf(RequestMethod.POST), consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun saveItem(model: Model, @RequestBody request: ModifyRequest3): String {
        // Get all items requested
        val items = itemRepository.findAllById(listOf(request.item_ids.id.toLong()))
        // Map changes to real items
        items.map { item ->
            item.copy(name = request.item_ids.name, price = request.item_ids.price.toDouble())
        }.also {
            // Save the items
            itemRepository.saveAll(it)
        }

        // Get the report id
        val requestId = request.report_id.first().toLong()
        val reports = notificationRepository.findAll() // Find all reports
                .filter { it.regularReports.isNotEmpty() } // Filter those that are concerning regularReports
                .map { it.regularReports } // Get only regular reports
                .filter {
                    // Filter those that have actual report inside them
                    it.map { it.id }.filterNotNull().contains(requestId)
                }.flatten() // Flatten
                .filter { it.id != requestId } // Remove actual report because we saved it

        regularReportRepository.deleteById(requestId) // Remove the report from database

        model.addAttribute("items", reports.sortedBy { it.id }) // Return reports sorted by id

        // Return page showNotification.html
        return "showNotification"
    }

    // Set item as advertised
    @RequestMapping("/addAdvert", method = arrayOf(RequestMethod.POST), consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun addAdvert(model: Model, @RequestBody request: ModifyRequest4): String {
        // Find all items requested
        val items = itemRepository.findAllById(request.item_ids.map { it.toLong() })

        items.map { item -> item.copy(advertise = true) } // Map items to enable advertising
                .also { itemRepository.saveAll(it) } // Save them

        val requestId = request.report_id.first().toLong() // Get actual request
        val reports = notificationRepository.findAll() // Find all reports
                .filter { it.regularReports.isNotEmpty() } // Filter those that are concerning regularReports
                .map { it.regularReports } // Get only regular reports
                .filter {
                    // Filter those that have actual report inside them
                    it.map { it.id }.filterNotNull().contains(requestId)
                }.flatten() // Flatten

        model.addAttribute("items", reports.sortedBy { it.id }) // Return reports sorted by id

        // Return page showNotification.html
        return "showNotification"
    }

    // Disable selling of selected items(s)
    @RequestMapping("/removeFromSelling", method = arrayOf(RequestMethod.POST), consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun removeFromSelling(model: Model, @RequestBody request: ModifyRequest4): String {
        // Find all items requested
        val items = itemRepository.findAllById(request.item_ids.map { it.toLong() })

        items.map { item -> item.copy(isSelling = false) } // Map items to disable selling
                .also { itemRepository.saveAll(it) } // Save them

        // Get the report id
        val requestId = request.report_id.first().toLong()
        val reports = notificationRepository.findAll() // Find all reports
                .filter { it.regularReports.isNotEmpty() } // Filter those that are concerning regularReports
                .map { it.regularReports } // Get only regular reports
                .filter {
                    // Filter those that have actual report inside them
                    it.map { it.id }.filterNotNull().contains(requestId)
                }.flatten() // Flatten
                .filter { it.id != requestId } // Remove actual report because we saved it

        regularReportRepository.deleteById(requestId) // Remove the report from database

        model.addAttribute("items", reports.sortedBy { it.id }) // Return reports sorted by id

        // Return page showNotification.html
        return "showNotification"
    }

    // Get actual advertisement
    @RequestMapping("/advert")
    fun showAdvert(model: Model): String {
        // Return all items that are currently selling and marked as advertising
        model.addAttribute("items", itemRepository.findAll().sortedBy { it.id }.filter { it.advertise && it.isSelling })

        // Return advert.html
        return "advert"
    }
}
