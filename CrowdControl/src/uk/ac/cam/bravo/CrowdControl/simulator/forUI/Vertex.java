package uk.ac.cam.bravo.CrowdControl.simulator.forUI;

import java.awt.geom.Point2D;
import java.io.Serializable;

public class Vertex extends Point2D.Float implements Serializable {

	private static final long serialVersionUID = 1L;

	public static enum EdgeType {wall(3f), connector(1f), furniture(1f);
		public final float lineWidth;
		private EdgeType(float w) {
			lineWidth = w;
		}
	};
	
	public final EdgeType edgeType;
	
	//needed to find out which polygon a connector vertex leads to
	public final int polyId;
	

	public Vertex(EdgeType e, int roomId) {
		super();
		edgeType = e;
		this.polyId = roomId;

	}

	public Vertex(float x, float y, EdgeType e, int roomId) {
		super(x, y);
		edgeType = e;
		this.polyId = roomId;

	}
	
	@Override
	public String toString() {
		String info = super.toString() + " edge type: " + edgeType.toString() + " (target=" + polyId + ")";
		return info;
	}
}
