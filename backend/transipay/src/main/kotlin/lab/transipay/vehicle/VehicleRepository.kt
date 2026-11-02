package lab.transipay.vehicle

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface VehicleRepository : JpaRepository<VehicleEntity, UUID> {

    fun existsByNumberPlate(numberPlate: String): Boolean

    fun existsByNumberPlateAndIdNot(numberPlate: String, id: UUID): Boolean

    fun findAllByCompanyId(companyId: UUID): List<VehicleEntity>

    @Query(value = "SELECT nextval('vehicle_code_seq')", nativeQuery = true)
    fun nextCodeNumber(): Long
}