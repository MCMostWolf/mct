package mct.region.anvil

import mct.MCTError
import okio.Path

sealed interface AnvilError : MCTError

sealed interface ConstructionError : AnvilError {
    data class DirNotFound(val dir: Path) : ConstructionError {
        override val message = "Trying to construct a manager on non-existing dir($dir) is not allowed"
    }
}

sealed interface LoadError : AnvilError {
    data class FileNotFound(val path: Path) : LoadError {
        override val message: String = "Region file not found: $path"
    }

    data class InvalidSize(val size: Long) : LoadError {
        override val message: String = "Region file too small ($size), must be >= 8KiB"
    }
}

sealed interface SaveError : AnvilError {
    data class FileNotFound(val path: Path) : SaveError {
        override val message: String = "Region file not found: $path"
    }
}