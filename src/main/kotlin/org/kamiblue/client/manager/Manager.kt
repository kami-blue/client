package org.kamiblue.client.manager

import org.kamiblue.client.util.Wrapper

interface Manager {
    val mc get() = Wrapper.minecraft
}