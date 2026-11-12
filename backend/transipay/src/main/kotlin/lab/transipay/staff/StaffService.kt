package lab.transipay.staff

import lab.transipay.auth.StaffPasswordEntity
import lab.transipay.auth.StaffPasswordRepository
import lab.transipay.common.exception.ConflictException
import lab.transipay.common.exception.NotFoundException
import lab.transipay.company.CompanyEntity
import lab.transipay.company.CompanyRepository
import lab.transipay.staff.dto.StaffRequest
import lab.transipay.staff.dto.StaffResponse
import lab.transipay.staff.dto.toResponse
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class StaffService(
    private val repository: StaffRepository,
    private val companyRepository: CompanyRepository,
    private val passwordRepository: StaffPasswordRepository,
    private val passwordEncoder: PasswordEncoder,
) {

    fun create(req: StaffRequest): StaffResponse {
        if (repository.existsByEmail(req.email))
            throw ConflictException("Email ${req.email} already in use")

        val staff = repository.save(
            StaffEntity(
                firstName = req.firstName,
                lastName = req.lastName,
                email = req.email,
                phone = req.phone,
                role = req.role,
                active = req.active,
                company = getCompanyOrThrow(req.companyId),
            )
        )

        req.password?.let {
            passwordRepository.save(
                StaffPasswordEntity(staff = staff, passwordHash = passwordEncoder.encode(it)!!)
            )
        }
        return staff.toResponse()
    }

    @Transactional(readOnly = true)
    fun findAll(companyId: UUID? = null): List<StaffResponse> =
        (if (companyId != null) repository.findAllByCompanyId(companyId) else repository.findAll())
            .map { it.toResponse() }

    @Transactional(readOnly = true)
    fun findOne(id: UUID): StaffResponse = getOrThrow(id).toResponse()

    fun update(id: UUID, req: StaffRequest): StaffResponse {
        val staff = getOrThrow(id)
        if (repository.existsByEmailAndIdNot(req.email, id))
            throw ConflictException("Email ${req.email} already in use")

        staff.apply {
            firstName = req.firstName
            lastName = req.lastName
            email = req.email
            phone = req.phone
            role = req.role
            active = req.active
            company = getCompanyOrThrow(req.companyId)
        }
        return staff.toResponse()
    }

    private fun getCompanyOrThrow(id: UUID): CompanyEntity =
        companyRepository.findById(id).orElseThrow { NotFoundException("Company $id not found") }

    private fun getOrThrow(id: UUID): StaffEntity =
        repository.findById(id).orElseThrow { NotFoundException("Staff $id not found") }
}