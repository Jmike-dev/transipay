package lab.transipay.company

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CompanyRepository : JpaRepository<CompanyEntity, UUID>