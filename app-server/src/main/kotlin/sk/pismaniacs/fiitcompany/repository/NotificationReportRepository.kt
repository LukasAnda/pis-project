package sk.pismaniacs.fiitcompany.repository

import org.springframework.data.jpa.repository.JpaRepository
import sk.pismaniacs.fiitcompany.model.RegularReport
import sk.pismaniacs.fiitcompany.model.SeasonalPriceReport
import sk.pismaniacs.fiitcompany.model.SeasonalReport

interface SeasonalReportsRepository : JpaRepository<SeasonalReport, Long>
interface RegularReportRepository : JpaRepository<RegularReport, Long>
interface SeasonalPriceReportRepository : JpaRepository<SeasonalPriceReport, Long>