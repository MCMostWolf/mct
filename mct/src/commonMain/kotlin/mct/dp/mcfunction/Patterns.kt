package mct.dp.mcfunction

val BuiltinPatterns = PatternSet {
    listOf("say", "me").forEach { cmd ->
        // say <message...>
        // me <action...>
        command(cmd) {
            Any() then {
                +GreedyPositions()
            }
        }
    }

    listOf("tell", "msg", "w", "teammsg").forEach { cmd ->
        // tell <targets> <message...>
        // msg <targets> <message...>
        // w <targets> <message...>
        // teammsg <message...>
        command(cmd) {
            WithSize(2) then {
                +GreedyPositions(2)
            }
        }
    }


    // tellraw <targets> <message>
    command("tellraw") {
        WithSize(2) then {
            +Positions(2)
        }
    }

    // title <targets> <action> <component>
    command("title") {
        WithSize(3, strict = true) then {
            Positions(3) then {
                Matches("not times") { cmd, _ ->
                    cmd[2].content != "times"
                }
            }
        }
    }

    // bossbar set <id> name <component>
    command("bossbar") {
        WithSize(4) then {
            Positions(4) then {
                Matches("bossbar name") { cmd, _ ->
                    cmd[1].content == "set" &&
                            cmd[3].content == "name"
                }
            }

        }
    }

    command("scoreboard") {
        // scoreboard objectives add <name> <criteria> <displayName>
        WithSize(4) then {
            Positions(4) then {
                Matches("objective display name") { cmd, _ ->
                    cmd[1].content == "objectives" &&
                            cmd[2].content == "add"
                }
            }
        }

        // scoreboard objectives modify <name> displayname <component>
        WithSize(4) then {
            Positions(4) then {
                Matches("objective modify displayname") { cmd, _ ->
                    cmd[1].content == "objectives" &&
                            cmd[2].content == "modify" &&
                            cmd[3].content == "displayname"
                }
            }
        }
    }

    command("team") {
        // team modify <team> prefix <component>
        // team modify <team> suffix <component>
        WithSize(4) then {
            Positions(4) then {
                Matches("team prefix/suffix") { cmd, _ ->
                    cmd[1].content == "modify" &&
                            (cmd[3].content == "prefix" || cmd[3].content == "suffix")
                }
            }
        }
    }

    command("data") {

        // data modify <target> <path> set value <json>
        Any() then {
            GreedyPositions(1) then {
                Matches("data modify value json") { cmd, arg ->
                    cmd.contains("modify") &&
                            cmd.contains("set") &&
                            cmd.contains("value") &&
                            arg.content.contains("text")
                }
            }
        }
    }


    command("give") {
        // give <targets> <item> [count] [components/NBT]
        Any() then {
            GreedyPositions(3) then {
                Matches("item name json") { _, arg ->
                    arg.content.contains("text")
                }
            }
        }
    }

    command("item") {
        // item modify <target> <slot> <modifier>
        // item replace <target> <slot> with <item>
        Any() then {
            GreedyPositions(3) then {
                Matches("item component json") { _, arg ->
                    arg.content.contains("text")
                }
            }
        }
    }
}






