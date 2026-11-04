package lab.transipay.payment

import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import java.util.UUID

interface PaymentRepository : JpaRepository<PaymentEntity, UUID> {

    // row lock so a duplicate/concurrent callback can't be processed twice
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByCheckoutRequestId(checkoutRequestId: String): PaymentEntity?

    fun findAllByStatus(status: PaymentStatus, pageable: Pageable): Page<PaymentEntity>

    fun findAllByVehicleId(vehicleId: UUID, pageable: Pageable): Page<PaymentEntity>
}