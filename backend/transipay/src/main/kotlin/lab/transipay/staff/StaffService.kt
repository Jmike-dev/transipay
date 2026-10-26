package lab.transipay.staff

import lab.transipay.common.exception.ConflictException
import lab.transipay.common.exception.NotFoundException
import lab.transipay.staff.dto.StaffRequest
import lab.transipay.staff.dto.StaffResponse
import lab.transipay.staff.dto.toResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class StaffService(private val repository: StaffRepository) {

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
        )
        return repository.save(staff).toResponse()
    }

    @Transactional(readOnly = true)
    fun findAll(): List<StaffResponse> = repository.findAll().map { it.toResponse() }

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
        }
        return staff.toResponse() // dirty checking saves automatically on commit
    }

    fun delete(id: UUID) {
        repository.delete(getOrThrow(id))
    }

    private fun getOrThrow(id: UUID): StaffEntity =
        repository.findById(id).orElseThrow { NotFoundException("Staff $id not found") }
}