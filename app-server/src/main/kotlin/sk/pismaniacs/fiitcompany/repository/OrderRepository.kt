package sk.pismaniacs.fiitcompany.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import sk.pismaniacs.fiitcompany.model.Purchase
import java.util.*

@Repository
interface OrderRepository : CrudRepository<Purchase, Long>