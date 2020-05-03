package sk.pismaniacs.fiitcompany.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import javax.persistence.*

@Entity
@Table(name = "seasonal_price_report")
data class SeasonalPriceReport(@OneToOne
                               @JoinColumn(name = "item_id")
                               @JsonIgnoreProperties("purchases", "seasons")
                               var item: Item? = null,
                               var priceChange: Double = 0.0,
                               @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                               var id: Long? = null)