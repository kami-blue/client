package org.kamiblue.client.setting

import org.kamiblue.client.KamiMod
import org.kamiblue.client.setting.configs.NameableConfig

internal object GenericConfig : NameableConfig<GenericConfigClass>(
    "generic",
    "${KamiMod.DIRECTORY}config/"
)