package sk.pismaniacs.fiitcompany.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import sk.pismaniacs.fiitcompany.model.Season

@Repository
interface SeasonRepository : CrudRepository<Season, Long>