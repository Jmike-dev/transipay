package lab.transipay.staff

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface StaffRepository : JpaRepository<StaffEntity, UUID> {
    fun existsByEmail(email: String): Boolean
    fun existsByEmailAndIdNot(email: String, id: UUID): Boolean
}