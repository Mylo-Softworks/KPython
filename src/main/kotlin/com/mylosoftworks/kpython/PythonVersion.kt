package com.mylosoftworks.kpython

enum class PythonVersion(val winVerName: String, val linuxVerName: String) {
    python37("python37", "python3.7"),
    python38("python38", "python3.8"),
    python39("python39", "python3.9"),
    python310("python310", "python3.10"),
    python311("python311", "python3.11"),
    python312("python312", "python3.12"),
    python313("python313", "python3.13");

    override fun toString() =
        if(System.getProperty("os.name").lowercase().contains("windows")) winVerName else linuxVerName
}