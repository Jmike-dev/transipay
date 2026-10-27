package lab.transipay.staff

import lab.transipay.common.exception.ConflictException
import lab.transipay.common.exception.NotFoundException
import lab.transipay.company.CompanyEntity
import lab.transipay.company.CompanyRepository
import lab.transipay.staff.dto.StaffRequest
import lab.transipay.staff.dto.StaffResponse
import lab.transipay.staff.dto.toResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class StaffService(
    private val repository: StaffRepository,
    private val companyRepository: CompanyRepository,
) {

    fun create(req: StaffRequest): StaffResponse {
        if (repository.existsByEmail(req.email))
            throw ConflictException("Email ${req.email} already in use")

        val staff = StaffEntity(
            firstName = req.firstName,
            lastName = req.lastName,
            email = req.email,
            phone = req.phone,
            role = req.role,
            active = req.active,
            company = getCompanyOrThrow(req.companyId),
        )
        return repository.save(staff).toResponse()
    }

    @Transactional(readOnly = true)
    fun findAll(companyId: UUID? = null): List<StaffResponse> =
        (if (companyId != null) repository.findAllByCompanyId(companyId) else repository.findAll())
            .map { it.toResponse() }

    // findOne and delete stay the same

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
    @Transactional(readOnly = true)
    fun findOne(id: UUID): StaffResponse = getOrThrow(id).toResponse()

    private fun getCompanyOrThrow(id: UUID): CompanyEntity =
        companyRepository.findById(id).orElseThrow { NotFoundException("Company $id not found") }

    private fun getOrThrow(id: UUID): StaffEntity =
        repository.findById(id).orElseThrow { NotFoundException("Staff $id not found") }
}