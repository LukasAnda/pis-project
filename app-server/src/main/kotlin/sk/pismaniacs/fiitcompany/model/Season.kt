package sk.pismaniacs.fiitcompany.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import javax.persistence.*

@Entity
@Table(name = "season")
data class Season(
        var name: String = "",
        var startDate: Long = 0L,
        var actual: Boolean = false,
        var editable: Boolean = false,
        @ManyToMany(fetch = FetchType.EAGER)
        @JoinTable(
                name = "season_item",
                joinColumns = arrayOf(JoinColumn(name = "season_id")),
                inverseJoinColumns = arrayOf(JoinColumn(name = "item_id"))
        )
        @JsonIgnoreProperties("purchases", "seasons")
        var items: List<Item> = emptyList(),
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null
)