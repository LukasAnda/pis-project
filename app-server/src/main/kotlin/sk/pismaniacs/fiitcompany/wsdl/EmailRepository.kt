package sk.pismaniacs.fiitcompany.wsdl

import sk.stuba.fiit.predmety.pis.pis.notificationservices.email.EmailService
import sk.stuba.fiit.predmety.pis.pis.notificationservices.email.types.Notify

object EmailRepository{
    private val emailPort = EmailService().emailPort
    fun sendEmail(){
        val emailResponse = emailPort.notify(Notify().apply {
            email = "lukas.anda@gmail.com"
            subject = "New Season"
            message = "Hey, new season arrived"
            teamId = "022"
            password = "Y6ZSLR"
        })
        if (emailResponse.isSuccess) {
            println("Email sent successfully")
        } else {
            println("Email failed to send")
        }
    }
}