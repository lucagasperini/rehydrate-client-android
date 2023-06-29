package com.lucagasperini.rehydrate

import com.androidplot.util.PixelUtils
import com.androidplot.xy.*
import java.text.Format

class PlotController(private val plot: XYPlot, title: String, xformat: Format) {
    init {
        // set a title (really unused for our purpose)
        plot.title.text = title

        // the plot format from Format.kt
        set_x_format(xformat)
        plot.graph.getLineLabelStyle(XYGraphWidget.Edge.LEFT).format = QuantityBarFormat()
    }

    // this function will update a format
    fun set_x_format(xformat: Format) {
        plot.graph.getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).format = xformat
    }

    // this function will clear the plot
    fun clear_plot() {
        plot.clear()
    }

    // this plot will be show as plot bar
    fun update_plot_bar(values_list: List<Int>, name: String, color: Int) {
        // setup x and y steps
        plot.setDomainStep(StepMode.INCREMENT_BY_VAL,1.0)
        plot.setRangeStep(StepMode.INCREMENT_BY_VAL,100.0)

        // format as bar
        val plan_series = SimpleXYSeries(values_list, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, name)
        val plan_bar_formatter = BarFormatter(color, color)
        plan_bar_formatter.setMarginLeft(PixelUtils.dpToPix(1F))
        plan_bar_formatter.setMarginRight(PixelUtils.dpToPix(1F))

        // add values on the plot
        plot.addSeries(plan_series, plan_bar_formatter)
        // setup renderer
        val renderer = plot.getRenderer(BarRenderer::class.java)
        renderer.setBarGroupWidth(BarRenderer.BarGroupWidthMode.FIXED_GAP, PixelUtils.dpToPix(5F))
        // redraw plot
        plot.redraw()
    }
    // this plot will be show as plot line
    fun update_plot_line(values_list: List<Int>, name: String, color: Int) {
        // setup x and y steps
        plot.setDomainStep(StepMode.INCREMENT_BY_VAL,1.0)
        plot.setRangeStep(StepMode.INCREMENT_BY_VAL,200.0)

        // format as line
        val plan_series = SimpleXYSeries(values_list, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, name)
        val plan_line_formatter = LineAndPointFormatter(color, color, null, null)

        // add values on the plot
        plot.addSeries(plan_series, plan_line_formatter)

        // redraw plot
        plot.redraw()
    }

}