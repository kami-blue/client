package org.kamiblue.client.setting

import org.kamiblue.client.KamiBlueMod
import org.kamiblue.client.setting.configs.NameableConfig

internal object GenericConfig : NameableConfig<GenericConfigClass>(
    "generic",
    "${KamiBlueMod.DIRECTORY}config/"
)