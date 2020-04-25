package sk.pismaniacs.fiitcompany.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import java.util.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment


@Controller
class WebController {
    lateinit var appMode: String
    @Autowired
    fun WebAppContoller(environment: Environment) {
        appMode = environment.getProperty("app-mode") ?: ""
    }
    @RequestMapping("/")
    fun index(model: Model): String {
        model.addAttribute("datetime", Date())
        model.addAttribute("username", "Peder")
        model.addAttribute("mode", appMode)
        return "index"
    }
}