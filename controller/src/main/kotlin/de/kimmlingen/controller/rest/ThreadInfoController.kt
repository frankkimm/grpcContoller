package de.kimmlingen.controller.rest

import de.kimmlingen.util.ThreadInfo
import de.kimmlingen.util.logger
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Profile("test")
@RestController
class ThreadInfoController {
    companion object {
        private val log = logger(ThreadInfoController::class)
    }

    @GetMapping(value = ["/thread"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @Throws(
        InterruptedException::class,
    )
    fun logThreadInfo(): ThreadInfo {
        val threadInfo: ThreadInfo = ThreadInfo.create(Thread.currentThread())
        log.trace(threadInfo.toString())
        // Sleep to simulate work.
        Thread.sleep(2000L)
        return threadInfo
    }
}
