package com.example.transipay.service.auth

object Session {
    var staffId: String? = null
    var companyId: String? = null
    var role: Role? = null
    var staffName: String = ""
    var companyName: String = ""

    val isAdmin: Boolean get() = role == Role.ADMIN

    fun start(r: AuthResponse) {
        staffId = r.staffId
        companyId = r.companyId
        role = r.roleEnum
        staffName = r.staffName.orEmpty()
        companyName = r.companyName.orEmpty()
    }

    fun clear() {
        staffId = null; companyId = null; role = null
        staffName = ""; companyName = ""
    }
}