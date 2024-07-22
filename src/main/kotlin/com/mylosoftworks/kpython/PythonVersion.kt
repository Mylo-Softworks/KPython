package com.mylosoftworks.kpython

enum class PythonVersion(val verName: String) {
    python37("python37"),
    python38("python38"),
    python39("python39"),
    python310("python310"),
    python311("python311"),
    python312("python312"),
    python313("python313");

    override fun toString() = verName
}