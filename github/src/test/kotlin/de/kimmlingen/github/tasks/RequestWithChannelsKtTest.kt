package de.kimmlingen.github.tasks

import de.kimmlingen.github.MockGithubService
import de.kimmlingen.github.concurrentProgressResults
import de.kimmlingen.github.createGitHubService
import de.kimmlingen.github.loadContributorsChannels
import de.kimmlingen.github.testRequestData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val KOTLIN_CONTRIBUTORS = 903

@OptIn(ExperimentalCoroutinesApi::class)
class RequestWithChannelsKtTest {
    @Test
    fun testChannels() =
        runTest {
            val startTime = currentTime
            var index = 0
            loadContributorsChannels(MockGithubService, testRequestData) {
                    users, _ ->
                val expected = concurrentProgressResults[index++]
                val time = currentTime - startTime
                assertEquals(expected.timeFromStart, time)
                assertEquals(expected.users, users)
            }
        }

    @Test
    @Ignore
    fun testChannelsReal() {
        runBlocking {
            val service = createGitHubService(testRequestData.username, testRequestData.password)
            launch(Dispatchers.Default) {
                loadContributorsChannels(service, testRequestData) { users, completed ->
                    if (completed) {
                        assertTrue { users.size > KOTLIN_CONTRIBUTORS }
                    }
                    println("users updated: ${users.size}")
                }
            }
        }
    }
}
