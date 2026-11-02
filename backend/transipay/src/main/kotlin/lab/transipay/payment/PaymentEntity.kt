package lab.transipay.payment

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import lab.transipay.vehicle.VehicleEntity
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "payment")
class PaymentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    var vehicle: VehicleEntity,

    @Column(name = "number_plate", nullable = false)
    var numberPlate: String,

    @Column(name = "phone_number", nullable = false)
    var phoneNumber: String,

    @Column(name = "amount", nullable = false)
    var amount: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: PaymentStatus = PaymentStatus.PENDING,

    @Column(name = "merchant_request_id")
    var merchantRequestId: String? = null,

    @Column(name = "checkout_request_id", unique = true)
    var checkoutRequestId: String? = null,

    @Column(name = "mpesa_receipt_number", unique = true)
    var mpesaReceiptNumber: String? = null,

    @Column(name = "result_code")
    var resultCode: Int? = null,

    @Column(name = "result_desc")
    var resultDesc: String? = null,

    @Column(name = "transaction_date")
    var transactionDate: Instant? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null,
)