package dev.aurakai.auraframefx.ai.task.execution

import dev.aurakai.auraframefx.ai.task.AITask
import dev.aurakai.auraframefx.ai.task.TaskStatus

/**
 * Genesis Task Execution Manager
 * Executes AI tasks and manages their lifecycle
 */
interface TaskExecutionManager {

    /**
     * Executes a task and returns the result
     */
    suspend fun executeTask(task: AITask): TaskResult

    /**
     * Executes multiple tasks in parallel
     */
    suspend fun executeTasks(tasks: List<AITask>): List<TaskResult>

    /**
     * Cancels a running task
     */
    suspend fun cancelTask(taskId: String): Boolean

    /**
     * Gets execution statistics
     */
    fun getExecutionStats(): ExecutionStats

    /**
     * Sets maximum concurrent tasks
     */
    fun setMaxConcurrentTasks(max: Int)

    /**
     * Gets current task queue size
     */
    fun getQueueSize(): Int
}

data class TaskResult(
    val taskId: String,
    val status: TaskStatus,
    val result: String? = null,
    val error: String? = null,
    val executionTimeMs: Long = 0L,
    val startTime: Long = 0L,
    val endTime: Long = 0L
)

data class ExecutionStats(
    val totalTasksExecuted: Int,
    val successfulTasks: Int,
    val failedTasks: Int,
    val averageExecutionTime: Double,
    val currentQueueSize: Int,
    val activeTasks: Int
)
