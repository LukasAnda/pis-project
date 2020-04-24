package sk.pismaniacs.fiitcompany.model

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonManagedReference
import javax.persistence.*

@Entity
@Table(name = "notification")
data class Notification(@OneToOne(fetch = FetchType.LAZY)
                        @JoinColumn(name = "item_id")
                        @JsonIgnoreProperties("purchases", "notification")
                        var item: Item? = null,
                        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                        var id: Long = 0)