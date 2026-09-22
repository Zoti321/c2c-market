package com.zoti321.c2cmarket.data.firebase

interface UserMappingGateway {
    suspend fun upsertBusinessUserId(businessUserId: String): Result<Unit>
}
