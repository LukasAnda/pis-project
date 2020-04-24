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
        @OneToMany(mappedBy = "item", orphanRemoval = true, cascade = arrayOf(CascadeType.ALL), fetch = FetchType.LAZY)
        @JsonIgnoreProperties("item")
        var purchases: List<Purchase> = emptyList(),
        @OneToOne(mappedBy = "item", orphanRemoval = true, cascade = arrayOf(CascadeType.ALL), fetch = FetchType.LAZY)
        @JsonIgnoreProperties("item")
        var notification: Notification? = null,
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long = 0
)