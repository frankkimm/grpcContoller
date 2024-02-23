package de.kimmlingen.github

import kotlinx.coroutines.delay
import retrofit2.Response

object MockGithubService : GitHubService {
    override suspend fun getOrgRepos(org: String): Response<List<Repo>> {
        delay(REPOS_DELAY)
        return Response.success(repos)
    }

    override suspend fun getRepoContributors(
        owner: String,
        repo: String,
    ): Response<List<User>> {
        val testRepo = reposMap.getValue(repo)
        delay(testRepo.delay)
        return Response.success(testRepo.users)
    }
}
