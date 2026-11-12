package lab.transipay.fees

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface FeesRepository : JpaRepository<FeesEntity, UUID> {
    fun findAllByCompanyId(companyId: UUID): List<FeesEntity>
}