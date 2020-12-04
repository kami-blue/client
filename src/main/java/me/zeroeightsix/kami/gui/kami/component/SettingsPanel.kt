package me.zeroeightsix.kami.gui.kami.component

import me.zeroeightsix.kami.gui.kami.Stretcherlayout
import me.zeroeightsix.kami.gui.kami.component.EnumButton.EnumbuttonIndexPoof
import me.zeroeightsix.kami.gui.kami.component.EnumButton.EnumbuttonIndexPoof.EnumbuttonInfo
import me.zeroeightsix.kami.gui.rgui.component.container.OrganisedContainer
import me.zeroeightsix.kami.gui.rgui.component.use.CheckButton
import me.zeroeightsix.kami.gui.rgui.component.use.CheckButton.CheckButtonPoof
import me.zeroeightsix.kami.gui.rgui.component.use.CheckButton.CheckButtonPoof.CheckButtonPoofInfo
import me.zeroeightsix.kami.gui.rgui.component.use.Slider
import me.zeroeightsix.kami.gui.rgui.component.use.Slider.SliderPoof
import me.zeroeightsix.kami.gui.rgui.component.use.Slider.SliderPoof.SliderPoofInfo
import me.zeroeightsix.kami.gui.rgui.render.theme.Theme
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.impl.BooleanSetting
import me.zeroeightsix.kami.setting.impl.EnumSetting
import me.zeroeightsix.kami.setting.impl.numerical.DoubleSetting
import me.zeroeightsix.kami.setting.impl.numerical.FloatSetting
import me.zeroeightsix.kami.setting.impl.numerical.IntegerSetting
import me.zeroeightsix.kami.setting.impl.numerical.NumberSetting
import me.zeroeightsix.kami.util.Bind
import org.kamiblue.commons.utils.DisplayEnum
import java.util.*

/**
 * Created by 086 on 6/08/2017.
 */
class SettingsPanel(theme: Theme?, module: Module?) : OrganisedContainer(theme, Stretcherlayout(1)) {

    var module: Module? = module
        set(value) {
            field = value
            minimumWidth = (parent.width * .9f).toInt()
            prepare()
            setAffectLayout(false)
            for (component in children) {
                component.width = width - 10
                component.x = 5
            }
        }

    private fun prepare() {
        getChildren().clear()
        val module = module

        if (module == null) {
            isVisible = false
            return
        }

        if (module.fullSettingList.isNotEmpty()) {
            for (setting in module.fullSettingList) {
                if (!setting.isVisible) continue
                val name = setting.name

                if (setting.value is Bind) {

                    @Suppress("UNCHECKED_CAST")
                    addChild(BindButton(setting.name, null, module, setting as Setting<Bind>))

                } else if (setting is NumberSetting<*>) {

                    val isBound = setting.isBound

                    // Terrible terrible bug fix.
                    // I know, these parseDoubles look awful, but any conversions I tried here would end up with weird floating point conversion errors.
                    // This is really the easiest solution..
                    val value = setting.value.toString().toDouble()
                    if (!isBound) {

                        val slider = UnboundSlider(value, name, setting is IntegerSetting)
                        slider.addPoof(object : SliderPoof<UnboundSlider, SliderPoofInfo>() {
                            override fun execute(component: UnboundSlider, info: SliderPoofInfo) {
                                when (setting) {
                                    is IntegerSetting -> setting.value = info.newValue.toInt()
                                    is FloatSetting -> setting.value = info.newValue.toFloat()
                                    is DoubleSetting -> setting.value = info.newValue
                                }
                            }
                        })
                        if (setting.max != null) slider.setMax(setting.max.toDouble())
                        if (setting.min != null) slider.setMin(setting.min.toDouble())
                        addChild(slider)

                    } else {

                        val min = setting.min.toDouble()
                        val max = setting.max.toDouble()
                        val step = if (setting.step != null) setting.step.toDouble()
                        else Slider.getDefaultStep(min, max)

                        val slider = Slider(value, min, max, step, name, setting is IntegerSetting)

                        slider.addPoof(object : SliderPoof<Slider, SliderPoofInfo>() {
                            override fun execute(component: Slider, info: SliderPoofInfo) {
                                when (setting) {
                                    is IntegerSetting -> setting.value = info.newValue.toInt()
                                    is FloatSetting -> setting.value = info.newValue.toFloat()
                                    is DoubleSetting -> setting.value = info.newValue
                                }
                            }
                        })
                        addChild(slider)

                    }

                } else if (setting is BooleanSetting) {

                    val checkButton = CheckButton(name, null)
                    checkButton.isToggled = setting.value
                    checkButton.addPoof(object : CheckButtonPoof<CheckButton, CheckButtonPoofInfo>() {
                        override fun execute(checkButton1: CheckButton, info: CheckButtonPoofInfo) {
                            if (info.action == CheckButtonPoofInfo.CheckButtonPoofInfoAction.TOGGLE) {
                                setting.value = checkButton.isToggled
                                this@SettingsPanel.module = module
                            }
                        }
                    })
                    addChild(checkButton)

                } else if (setting is EnumSetting<*>) {

                    val enumClass = setting.clazz
                    val enumValues = enumClass.enumConstants
                    val modes = enumValues.map { if (it is DisplayEnum) it.displayName else it.name }.toTypedArray()
                    val enumButton = EnumButton(name, null, modes)

                    enumButton.addPoof(object : EnumbuttonIndexPoof<EnumButton, EnumbuttonInfo>() {
                        override fun execute(component: EnumButton, info: EnumbuttonInfo) {
                            setting.value = enumValues[info.getNewIndex()]
                            this@SettingsPanel.module = module
                        }
                    })

                    enumButton.setIndex(enumValues.indexOf(setting.value))
                    addChild(enumButton)

                }
            }
        }
        isVisible = children.isNotEmpty()
    }
}