package mct.region

import io.kotest.assertions.arrow.core.shouldNotRaise
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import mct.pointer.DataPointer
import mct.pointer.decodeFromString
import mct.pointer.encodeToString

class NbtExtractPatternTest : FreeSpec({
    "match test" - {
        listOf(
            ">#>#Entities>0>#FireworksItem>#tag>#display>#Name",
            ">#>#Entities>0>#CustomName",
            ">#>#block_entities>5>#front_text>#messages>0>#raw"
        ).forEach { ptr ->
            val ptr = shouldNotRaise {
                DataPointer.decodeFromString(ptr)
            }
            "BUILTIN_SET should match ${ptr.encodeToString()}" {
                BuiltinRegionPatterns.any { it.match(ptr) } shouldBe true
            }
        }
    }
})
