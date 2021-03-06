package uk.ac.cam.bravo.CrowdControl.simulator.forUI;

import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.List;

public interface RoomShape extends Serializable {
	/**
	 * Returns this room shape's vertices grouped by polygon.
	 * 
	 * @return the room shape's vertices
	 */
	public List<List<Vertex>> getEdges();

	/**
	 * Returns this room shape's name.
	 * 
	 * @return this room shape's name.
	 */
	public String getName();

	/**
	 * Returns the number of the floor this room shape is on.
	 * 
	 * @return this room shape's floor number
	 */
	public int getFloor();

	public Rectangle2D.Float getBoundingBox();

	public boolean[][] getPassableMap();
}
