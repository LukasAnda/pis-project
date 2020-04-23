package sk.pismaniacs.fiitcompany.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import sk.pismaniacs.fiitcompany.model.Item

@Repository
interface ItemRepository : CrudRepository<Item, Long> {

    fun findByName(name: String): Iterable<Item>
}