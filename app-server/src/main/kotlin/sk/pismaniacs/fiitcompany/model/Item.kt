package sk.pismaniacs.fiitcompany.model

import org.hibernate.annotations.GenericGenerator
import javax.persistence.*

@Entity
@Table(name = "item")
data class Item(
        var name: String = "",
        var price: Double = 0.0,
        @OneToMany(mappedBy = "item", orphanRemoval = true, cascade = arrayOf(CascadeType.ALL), fetch = FetchType.EAGER)
        var purchases: List<Purchase> = emptyList(),
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long = 0
)