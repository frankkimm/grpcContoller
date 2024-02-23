package de.kimmlingen.util

data class ThreadInfo(val name: String, val id: Long, val virtual: Boolean) {
    companion object {
        fun create(thread: Thread): ThreadInfo {
            return ThreadInfo(thread.name, thread.threadId(), thread.isVirtual)
        }
    }
}
