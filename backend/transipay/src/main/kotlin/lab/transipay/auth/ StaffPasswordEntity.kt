package lab.transipay.auth

import jakarta.persistence.*
import lab.transipay.staff.StaffEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "staff_password")
class StaffPasswordEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staff_id", nullable = false, unique = true)
    var staff: StaffEntity,

    @Column(name = "password_hash", nullable = false)
    var passwordHash: String,

    @Column(name = "failed_attempts", nullable = false)
    var failedAttempts: Int = 0,

    @Column(name = "locked_until")
    var lockedUntil: Instant? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
) {
    @PreUpdate
    fun onUpdate() {
        updatedAt = Instant.now()
    }
}

interface StaffPasswordRepository : JpaRepository<StaffPasswordEntity, UUID> {
    fun findByStaffId(staffId: UUID): StaffPasswordEntity?
}