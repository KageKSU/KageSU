package com.kageksu.kagesu.data.repository

import com.kageksu.kagesu.data.model.Module
import com.kageksu.kagesu.data.model.ModuleUpdateInfo

interface ModuleRepository {
    suspend fun getModules(): Result<List<Module>>
    suspend fun checkUpdate(module: Module): Result<ModuleUpdateInfo>
}
