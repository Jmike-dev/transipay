package lab.transipay.vehicle

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import lab.transipay.company.CompanyEntity
import java.util.UUID

@Entity
@Table(name = "vehicle")
class VehicleEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(name = "code", nullable = false, unique = true, updatable = false)
    var code: String,

    @Column(name = "number_plate", nullable = false, unique = true)
    var numberPlate: String,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    var company: CompanyEntity,
)