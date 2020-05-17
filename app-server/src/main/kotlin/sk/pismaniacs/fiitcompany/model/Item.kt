package sk.pismaniacs.fiitcompany.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonManagedReference
import org.hibernate.annotations.GenericGenerator
import javax.persistence.*

@Entity
@Table(name = "item")
data class Item(
        var name: String = "",
        var price: Double = 0.0,
        @OneToMany(mappedBy = "item", orphanRemoval = true, cascade = arrayOf(CascadeType.ALL), fetch = FetchType.EAGER)
        @JsonIgnoreProperties("item")
        var purchases: List<Purchase> = emptyList(),
        var advertise: Boolean = false,
        var isSelling: Boolean = true,
        @ManyToMany(mappedBy = "items")
        @JsonIgnoreProperties("items")
        var seasons: List<Season> = emptyList(),
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null
)