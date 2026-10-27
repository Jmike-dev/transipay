package lab.transipay.company

import lab.transipay.common.exception.ConflictException
import lab.transipay.common.exception.NotFoundException
import lab.transipay.company.dto.CompanyRequest
import lab.transipay.company.dto.CompanyResponse
import lab.transipay.company.dto.toResponse
import lab.transipay.staff.StaffRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class CompanyService(
    private val repository: CompanyRepository,
    private val staffRepository: StaffRepository,
) {

    fun create(req: CompanyRequest): CompanyResponse {
        val company = CompanyEntity(companyName = req.companyName.trim())
        return repository.save(company).toResponse()
    }

    @Transactional(readOnly = true)
    fun findAll(): List<CompanyResponse> = repository.findAll().map { it.toResponse() }

    @Transactional(readOnly = true)
    fun findOne(id: UUID): CompanyResponse = getOrThrow(id).toResponse()

    fun update(id: UUID, req: CompanyRequest): CompanyResponse {
        val company = getOrThrow(id)
        company.companyName = req.companyName.trim()
        return company.toResponse() // dirty checking saves on commit
    }

    fun delete(id: UUID) {
        val company = getOrThrow(id)
        if (staffRepository.existsByCompanyId(id))
            throw ConflictException("Company $id still has staff and cannot be deleted")
        repository.delete(company)
    }

    private fun getOrThrow(id: UUID): CompanyEntity =
        repository.findById(id).orElseThrow { NotFoundException("Company $id not found") }
}