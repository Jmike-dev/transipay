package lab.transipay.fees

import lab.transipay.company.CompanyRepository
import lab.transipay.fees.dto.CreateFeeRequest
import lab.transipay.fees.dto.FeeResponse
import lab.transipay.fees.dto.UpdateFeeRequest
import lab.transipay.fees.dto.toResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class FeesService(
    private val feesRepository: FeesRepository,
    private val companyRepository: CompanyRepository,
) {

    @Transactional
    fun create(request: CreateFeeRequest): FeeResponse {
        if (!companyRepository.existsById(request.companyId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Company ${request.companyId} not found")
        }
        val fee = FeesEntity(companyId = request.companyId, amount = request.amount)
        return feesRepository.save(fee).toResponse()
    }

    @Transactional(readOnly = true)
    fun findAll(): List<FeeResponse> =
        feesRepository.findAll().map { it.toResponse() }

    @Transactional(readOnly = true)
    fun findById(id: UUID): FeeResponse =
        getOrThrow(id).toResponse()

    @Transactional(readOnly = true)
    fun findByCompany(companyId: UUID): List<FeeResponse> =
        feesRepository.findAllByCompanyId(companyId).map { it.toResponse() }

    @Transactional
    fun update(id: UUID, request: UpdateFeeRequest): FeeResponse {
        val fee = getOrThrow(id)
        fee.amount = request.amount
        return feesRepository.save(fee).toResponse()
    }

    @Transactional
    fun delete(id: UUID) {
        feesRepository.delete(getOrThrow(id))
    }

    private fun getOrThrow(id: UUID): FeesEntity =
        feesRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Fee $id not found")
        }
}