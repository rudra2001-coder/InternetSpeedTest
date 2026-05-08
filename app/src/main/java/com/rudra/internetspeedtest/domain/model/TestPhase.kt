package com.rudra.internetspeedtest.domain.model

enum class TestPhase {
    IDLE,
    PING,
    DOWNLOAD,
    UPLOAD,
    COMPLETE,
    FAILED
}
