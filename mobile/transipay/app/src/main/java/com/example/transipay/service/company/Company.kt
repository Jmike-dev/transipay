package com.example.transipay.service.company

data class Company(
    val id : String,
    val companyName: String
)

data class CreateCompanyRequest(
    val companyName: String
)

data class UpdateCompanyRequest(
    val id: String,
    val companyName: String
)