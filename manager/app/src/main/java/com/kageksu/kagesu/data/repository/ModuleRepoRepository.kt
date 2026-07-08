package com.kageksu.kagesu.data.repository

import com.kageksu.kagesu.data.model.RepoModule

interface ModuleRepoRepository {
    suspend fun fetchModules(): Result<List<RepoModule>>
}
