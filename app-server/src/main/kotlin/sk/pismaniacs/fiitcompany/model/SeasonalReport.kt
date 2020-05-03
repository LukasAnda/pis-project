package sk.pismaniacs.fiitcompany.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import javax.persistence.*

@Entity
@Table(name = "seasonal_report")
data class SeasonalReport(@OneToOne
                               @JoinColumn(name = "item_id")
                               @JsonIgnoreProperties("purchases", "seasons")
                               var item: Item? = null,
                               @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                               var id: Long? = null)