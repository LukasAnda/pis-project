package sk.pismaniacs.fiitcompany.model

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonManagedReference
import javax.persistence.*

@Entity
@Table(name = "regular_report")
data class RegularReport(@OneToOne(fetch = FetchType.EAGER)
                         @JoinColumn(name = "item_id")
                         @JsonIgnoreProperties("purchases", "seasons")
                         var item: Item? = null,
                         var priceChange: Double = 0.0,
                         @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                         var id: Long? = null)