package org.kamiblue.client.event.events

import org.kamiblue.client.event.Event
import org.kamiblue.client.util.graphics.VertexHelper

class RenderRadarEvent(
    val vertexHelper: VertexHelper,
    val radius: Float,
    val scale: Float
) : Event