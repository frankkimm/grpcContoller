package de.kimmlingen.github

import de.kimmlingen.github.contributors.logRepos
import de.kimmlingen.github.contributors.logUsers
import de.kimmlingen.github.tasks.aggregate
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import retrofit2.Response

suspend fun loadContributorsChannels(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit,
) = coroutineScope {
    val repos =
        service
            .getOrgRepos(req.org)
            .also { logRepos(req, it) }
            .bodyList()

    val channel = Channel<List<User>>()
    for (repo in repos) {
        launch {
            val users =
                service.getRepoContributors(req.org, repo.name)
                    .also { logUsers(repo, it) }
                    .bodyList()
            channel.send(users)
        }
    }
    var allUsers = emptyList<User>()
    repeat(repos.size) {
        val users = channel.receive()
        allUsers = (allUsers + users).aggregate()
        updateResults(allUsers, it == repos.lastIndex)
    }
}

fun <T> Response<List<T>>.bodyList(): List<T> {
    return body() ?: listOf()
}
