package lab.transipay.staff

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import lab.transipay.staff.dto.StaffRequest
import lab.transipay.staff.dto.StaffResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/staff")
@Tag(name = "Staff", description = "Staff management")
class StaffController(private val service: StaffService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a staff member")
    fun create(@Valid @RequestBody req: StaffRequest): StaffResponse = service.create(req)

    @GetMapping
    @Operation(summary = "List all staff")
    fun findAll(): List<StaffResponse> = service.findAll()

    @GetMapping("/{id}")
    @Operation(summary = "Get a staff member by id")
    fun findOne(@PathVariable id: UUID): StaffResponse = service.findOne(id)

    @PutMapping("/{id}")
    @Operation(summary = "Update a staff member")
    fun update(@PathVariable id: UUID, @Valid @RequestBody req: StaffRequest): StaffResponse =
        service.update(id, req)
//
//    @DeleteMapping("/{id}")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    @Operation(summary = "Delete a staff member")
//    fun delete(@PathVariable id: UUID) = service.delete(id)
}