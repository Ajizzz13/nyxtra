package com.nyxtra.vpn.data.model

data class TrafficStats(
    val uploadBps: Long = 0L,
    val downloadBps: Long = 0L,
    val totalUploadBytes: Long = 0L,
    val totalDownloadBytes: Long = 0L
) {
    fun formatUploadSpeed(): String = formatSpeed(uploadBps)
    fun formatDownloadSpeed(): String = formatSpeed(downloadBps)
    fun formatTotalUpload(): String = formatBytes(totalUploadBytes)
    fun formatTotalDownload(): String = formatBytes(totalDownloadBytes)

    companion object {
        fun formatSpeed(bps: Long): String {
            if (bps < 1024) return "$bps B/s"
            val kb = bps / 1024.0
            if (kb < 1024) return String.format("%.1f KB/s", kb)
            val mb = kb / 1024.0
            return String.format("%.2f MB/s", mb)
        }

        fun formatBytes(bytes: Long): String {
            if (bytes < 1024) return "$bytes B"
            val kb = bytes / 1024.0
            if (kb < 1024) return String.format("%.1f KB", kb)
            val mb = kb / 1024.0
            if (mb < 1024) return String.format("%.1f MB", mb)
            val gb = mb / 1024.0
            return String.format("%.2f GB", gb)
        }
    }
}
