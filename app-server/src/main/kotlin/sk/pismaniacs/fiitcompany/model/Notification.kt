package sk.pismaniacs.fiitcompany.model

import org.hibernate.annotations.LazyCollection
import org.hibernate.annotations.LazyCollectionOption
import javax.persistence.*

@Entity
@Table(name = "notification")
data class Notification (
        var message: String = "",
        var actual: Boolean = false,
        @LazyCollection(LazyCollectionOption.FALSE)
        @OneToMany
        @JoinColumn(name = "regular_report_id")
        var regularReports: List<RegularReport> = emptyList(),
        @LazyCollection(LazyCollectionOption.FALSE)
        @OneToMany
        @JoinColumn(name = "seasonal_price_report_id")
        var seasonalPriceReports: List<SeasonalPriceReport> = emptyList(),
        @LazyCollection(LazyCollectionOption.FALSE)
        @OneToMany
        @JoinColumn(name = "seasonal_report_id")
        var seasonalReports: List<SeasonalReport> = emptyList(),
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null
)