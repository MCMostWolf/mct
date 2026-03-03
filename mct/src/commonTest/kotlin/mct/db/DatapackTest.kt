package mct.db

import io.kotest.assertions.arrow.core.shouldNotRaise
import io.kotest.assertions.fail
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import mct.Extraction
import mct.MCTWorkspace
import mct.Replacement
import mct.dp.backfill
import mct.dp.extractTextMCF
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import okio.use

class DatapackTest : StringSpec({
    fun extractWithoutError(mcf: String) = shouldNotRaise {
        extractTextMCF(mcf, "test", "test").extractions
    }

    val TEST_MCF = """
                tellraw @a [{"storage":"global","nbt":"Prefix.ERROR"},{"text":"飛距離が設定されていない！"}]
                
                should_be_ignore {"reason": "No a TextCompound"}
                # ignore_again "Ciallo"
                // although illegally for mcf, should be included: "ABC"
                pointer modify entity @e[type=item,limit=1] Item.tag.display.Lore append value '{"text":"[Level 99]","color":"dark_red"}'
                
                tellraw @s {"text":"compact","extra":[{"text":"text"}]}
                complex "item_id_001" b{strength:50b, durability:100s}
                
                # test escape
                pointer modify storage asset:artifact Name set value '{"text":"アンク\'s Fury","color":"#FF5555","bold":true}'
                tell @a "\"Kukayo\": {\"text\": \"A text compound is like this\"} 🫧"
              """.trimIndent()

    "test extract" {
        val extraction = extractWithoutError(TEST_MCF)
        extraction.forEach {
            it as? Extraction.Datapack.MCFunction shouldNotBeNull {
                println(this)
                content shouldBeEqual TEST_MCF.substring(indices)
            }
        }
        extraction.size shouldBeEqual 7
    }

    "test backfill" {
        val extraction = extractWithoutError(TEST_MCF)
        val replacements = extraction.map {
            when(it) {
                is Extraction.Datapack.MCFunction -> Replacement.Datapack.MCFunction(it.indices, "{CIALLO}")
                is Extraction.Datapack.MCJson -> fail("Should not reach")
            }
        }
        val backfilled = TEST_MCF.backfill(replacements)
        """
                tellraw @a {CIALLO}
                
                should_be_ignore {"reason": "No a TextCompound"}
                # ignore_again "Ciallo"
                // although illegally for mcf, should be included: {CIALLO}
                pointer modify entity @e[type=item,limit=1] Item.tag.display.Lore append value {CIALLO}
                
                tellraw @s {CIALLO}
                complex {CIALLO} b{strength:50b, durability:100s}
                
                # test escape
                pointer modify storage asset:artifact Name set value {CIALLO}
                tell @a {CIALLO}
        """.trimIndent() shouldBeEqual backfilled
    }

    fun mockWorkspace(block: (MCTWorkspace) -> Unit) {
        return FakeFileSystem().use { fs ->
            val datapackDir = "datapack".toPath()
            fs.createDirectory(datapackDir)
        }
    }

    "test extract from workspace" {
        mockWorkspace { workspace ->
            workspace.datapackDir
            workspace.fs.openReadWrite(workspace.datapackDir)
        }
    }
})