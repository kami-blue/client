package me.zeroeightsix.kami.setting

import org.kamiblue.client.KamiMod
import me.zeroeightsix.kami.setting.configs.NameableConfig

internal object GenericConfig : NameableConfig<GenericConfigClass>(
    "generic",
    "${KamiMod.DIRECTORY}config/"
)