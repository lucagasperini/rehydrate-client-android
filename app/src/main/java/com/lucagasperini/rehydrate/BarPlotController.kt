package com.lucagasperini.rehydrate

import com.androidplot.util.PixelUtils
import com.androidplot.xy.*
import java.text.Format
import java.util.*

class PlotController(val plot: XYPlot, val title: String, xformat: Format) {
    init {
        plot.title.text = title
        plot.setUserRangeOrigin(0.0)
        plot.setRangeLowerBoundary(0,BoundaryMode.FIXED)

        plot.setDomainStep(StepMode.INCREMENT_BY_VAL,1.0)
        plot.setRangeStep(StepMode.INCREMENT_BY_VAL,50.0)
        set_x_format(xformat)
        plot.graph.getLineLabelStyle(XYGraphWidget.Edge.LEFT).format = QuantityBarFormat()
    }

    fun set_domain_label_distance(dp: Float) {
        plot.graph.lineLabelInsets.bottom = PixelUtils.dpToPix(dp);
    }

    fun set_x_format(xformat: Format) {
        plot.graph.getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).format = xformat;
    }

    fun clear_plot() {
        plot.clear()
    }

    fun update_plot_bar(values_list: List<Int>, name: String, color: Int) {
        val plan_series = SimpleXYSeries(values_list, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, name);
        var plan_bar_formatter = BarFormatter(color, color)
        plan_bar_formatter.setMarginLeft(PixelUtils.dpToPix(1F));
        plan_bar_formatter.setMarginRight(PixelUtils.dpToPix(1F));


        plot.addSeries(plan_series, plan_bar_formatter)
        val renderer = plot.getRenderer(BarRenderer::class.java)
        renderer.setBarGroupWidth(BarRenderer.BarGroupWidthMode.FIXED_GAP, PixelUtils.dpToPix(5F))

        plot.redraw()
    }

    fun update_plot_line(values_list: List<Int>, name: String, color: Int) {
        val plan_series = SimpleXYSeries(values_list, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, name);
        var plan_line_formatter = LineAndPointFormatter(color, color, null, null)

        plot.addSeries(plan_series, plan_line_formatter)


        plot.redraw()
    }

}