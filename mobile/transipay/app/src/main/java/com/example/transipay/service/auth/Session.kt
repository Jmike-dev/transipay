package com.example.transipay.service.auth

object Session {
    var staffId: String? = null
    var companyId: String? = null
    var role: Role? = null

    val isAdmin: Boolean get() = role == Role.ADMIN

    fun start(r: AuthResponse) {
        staffId = r.staffId
        companyId = r.companyId
        role = r.roleEnum
    }

    fun clear() {
        staffId = null; companyId = null; role = null
    }
}