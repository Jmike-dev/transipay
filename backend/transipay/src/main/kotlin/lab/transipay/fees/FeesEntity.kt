package lab.transipay.fees

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "fees")
class FeesEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(name = "company_id", nullable = false)
    var companyId: UUID,

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    var amount: BigDecimal,
)