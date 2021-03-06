package sk.pismaniacs.fiitcompany.model

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.hibernate.annotations.GenericGenerator
import javax.persistence.*

@Entity
@Table(name = "purchase")
data class Purchase(
        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "item_id")
        @JsonIgnoreProperties("purchases", "seasons")
        var item: Item? = null,
        var quantity: Int = 0,
        var dateOfPurchase: Long = 0L,
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null
)