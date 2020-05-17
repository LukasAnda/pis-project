package sk.pismaniacs.fiitcompany.wsdl

import sk.stuba.fiit.predmety.pis.pis.notificationservices.mail.MailService
import sk.stuba.fiit.predmety.pis.pis.notificationservices.mail.types.Notify

object MailRepository {
    private var mailPort = MailService().mailPort

    fun sendMail(){
        val mailResponse = mailPort.notify(Notify().apply {
            address = "Ilkovicova 2"
            subject = "New Season"
            message = "Hey, new season arrived"
            teamId = "022"
            password = "Y6ZSLR"
        })
        if (mailResponse.isSuccess) {
            println("Mail sent successfully")
        } else {
            println("Mail failed to send")
        }
    }
}