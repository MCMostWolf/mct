@file:Suppress("FunctionName")

package mct.region

import mct.pointer.PatternSet
import mct.pointer.RegexPattern
import mct.pointer.RightPattern

val BuiltinPatterns = PatternSet {
    // --- Item Display & Lore (Legacy/General) ---
    +RightPattern("#display>#Name")                   // Item custom name
    +RightPattern("#display>#Lore")                   // Item lore lines

    // --- Modern Item Components (1.20.5+) ---
    // In region files, these are often nested within an item's 'components' tag
    +RightPattern("#components>#minecraft:custom_name")
    +RegexPattern("""#components>#minecraft:lore>\d+$""")

    // --- Written Books (Nested in Item Tags) ---
    +RightPattern("#Book>#tag>#pages")                // Book page content
    +RightPattern("#Book>#tag>#title")                // Book title
    +RightPattern("#Book>#tag>#author")               // Book author
    +RightPattern("#Book>#tag>#filtered_pages")       // Censored/Filtered pages
    +RightPattern("#Book>#tag>#filtered_title")       // Censored/Filtered title

    // --- Entities (Mobs, Armor Stands, etc.) ---
    // Matches CustomName for all entities stored in the chunk
    +RegexPattern("""^>#>#Entities>\d+>#CustomName$""")

    // --- Block Entities (Signs, Containers, Spawners) ---
    // 1. Signs (Front & Back)
    listOf("front_text", "back_text").forEach { side ->
        +RegexPattern("^>#>#block_entities>\\d+>#$side>#messages$")
        +RegexPattern("^>#>#block_entities>\\d+>#$side>#filtered_messages$")
    }

    // 2. Container Names (Chests, Shulker Boxes, Hoppers)
    // These use 'CustomName' at the block entity root
    +RegexPattern("""^>#>#block_entities>\d+>#CustomName$""")

    // 3. Command Blocks
    // 'CustomName' is the name shown in chat, 'Command' is the actual logic
    +RegexPattern("""^>#>#block_entities>\d+>#CustomName$""")
    // Note: Translating 'Command' is risky, but sometimes hoverEvent/show_text inside commands needs it
    // +RegexPattern("""^>#>#block_entities>\d+>#Command$""")

    // 4. Spawners
    // Potential custom names for spawned entities inside a spawner
    +RegexPattern("""^>#>#block_entities>\d+>#SpawnData>#entity>#CustomName$""")

    // --- Map Data ---
    // If scanning 'data/map_xxx.dat' files (though usually in separate folder)
    +RightPattern("#banners>#name")                   // Names of marked banners on maps
}