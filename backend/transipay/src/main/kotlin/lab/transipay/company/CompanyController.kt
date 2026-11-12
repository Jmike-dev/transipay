package lab.transipay.company

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import lab.transipay.company.dto.CompanyRequest
import lab.transipay.company.dto.CompanyResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/companies")
@Tag(name = "Company", description = "Company management")
class CompanyController(private val service: CompanyService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a company")
    fun create(@Valid @RequestBody req: CompanyRequest): CompanyResponse = service.create(req)

    @GetMapping
    @Operation(summary = "List all companies")
    fun findAll(): List<CompanyResponse> = service.findAll()

    @GetMapping("/{id}")
    @Operation(summary = "Get a company by id")
    fun findOne(@PathVariable id: UUID): CompanyResponse = service.findOne(id)

    @PutMapping("/{id}")
    @Operation(summary = "Update a company")
    fun update(@PathVariable id: UUID, @Valid @RequestBody req: CompanyRequest): CompanyResponse =
        service.update(id, req)

//    @DeleteMapping("/{id}")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    @Operation(summary = "Delete a company")
//    fun delete(@PathVariable id: UUID) = service.delete(id)
}