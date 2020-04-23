package sk.pismaniacs.fiitcompany.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import sk.pismaniacs.fiitcompany.model.Purchase

@Repository
interface OrderRepository : CrudRepository<Purchase, Long>