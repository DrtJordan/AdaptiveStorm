package ruc.edu.tools;

import java.awt.BasicStroke;
import java.awt.Stroke;

import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

public class MXYLineAndShapeRender extends XYLineAndShapeRenderer{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4329371709147971220L;
	private Stroke stroke = null;
	
	public MXYLineAndShapeRender( boolean isDashed) {
		stroke = isDashed ? new BasicStroke(4.0f,BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_MITER, 10.0f, new float[] {10.0f}, 0.0f) : new BasicStroke(4.0f);
		setShapesVisible(false);
	}
	
	@Override
	public Stroke getItemStroke(int row, int column) {
		return stroke;
	}
}
