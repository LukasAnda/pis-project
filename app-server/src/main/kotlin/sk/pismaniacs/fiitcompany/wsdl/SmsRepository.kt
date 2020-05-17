package sk.pismaniacs.fiitcompany.wsdl

import org.springframework.beans.factory.annotation.Autowired
import sk.stuba.fiit.predmety.pis.pis.notificationservices.sms.SMSService
import sk.stuba.fiit.predmety.pis.pis.notificationservices.sms.types.Notify

object SmsRepository {
    private var smsPort = SMSService().smsPort

    fun sendSms(){
        val smsResponse = smsPort.notify(Notify().apply {
            phone = "0918999000"
            subject = "New Season"
            message = "Hey, new season arrived"
            teamId = "022"
            password = "Y6ZSLR"
        })

        if(smsResponse.isSuccess){
            println("SMS sent sucessfully")
        } else {
            println("SMS failed to send")
        }
    }
}