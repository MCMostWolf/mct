@file:Suppress("FunctionName")

package mct.region

import mct.pointer.PatternSet
import mct.pointer.RegexPattern
import mct.pointer.RightPattern

val BuiltinPatterns = PatternSet {
    +RightPattern("#display>#Name")
    +RightPattern("#display>#Lore")
    +RightPattern("#Book>#tag>#pages")
    +RightPattern("#Book>#tag>#title")
    +RightPattern("#Book>#tag>#author")
    +RightPattern("#Book>#tag>#filtered_pages")
    +RightPattern("#Book>#tag>#filtered_title")
    +RegexPattern("""^>#>#Entities>\d+>#CustomName$""")
    listOf("front_text", "back_text").forEach {
        +RegexPattern("^>#>#block_entities>\\d+>#$it>#messages$")
        +RegexPattern("^>#>#block_entities>\\d+>#$it>#filtered_messages$")
    }
}