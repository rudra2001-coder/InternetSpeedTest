package com.rudra.internetspeedtest.core.testing

data class TestAuditEntry(
    val timestamp: Long,
    val phase: String,
    val detail: String,
    val metrics: Map<String, Double> = emptyMap()
)

class TestAuditor {
    private val auditLog = mutableListOf<TestAuditEntry>()

    fun record(phase: String, detail: String, metrics: Map<String, Double> = emptyMap()) {
        auditLog.add(TestAuditEntry(System.currentTimeMillis(), phase, detail, metrics))
    }

    fun getLog(): List<TestAuditEntry> = auditLog.toList()

    fun exportFullAudit(): String {
        val sb = StringBuilder()
        sb.appendLine("=== Complete Test Audit Log ===")
        sb.appendLine()
        auditLog.forEachIndexed { i, entry ->
            sb.appendLine("#${i + 1}: [${entry.phase}] ${entry.detail}")
            if (entry.metrics.isNotEmpty()) {
                entry.metrics.forEach { (k, v) ->
                    sb.appendLine("   $k: ${String.format("%.2f", v)}")
                }
            }
        }
        return sb.toString()
    }

    fun reset() {
        auditLog.clear()
    }
}
