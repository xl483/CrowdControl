package uk.ac.cam.bravo.CrowdControl.simulator;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import uk.ac.cam.bravo.CrowdControl.simulator.forAgent.DoorInterface;

class BuildingGraph {

	// A weighted element in a priority queue

	private class Element<T> implements Comparable<Element<T>> {
		int weight;
		T elem;

		float straightLineDistSqr;
		
		public Element(int weight, T elem) {
			this.weight = weight;
			this.elem = elem;
			this.straightLineDistSqr = 0f;
		}
		
		public Element(int weight, T elem, float dist) {
			this.weight = weight;
			this.elem = elem;
			this.straightLineDistSqr = dist;
		}

	
		@Override
		public boolean equals(Object obj) {
			return obj instanceof Element<?>
					&& elem.equals(((Element<?>) obj).elem);
		}

		@Override
		public int hashCode() {
			return elem.hashCode();
		}


		@Override
		public int compareTo(Element<T> o) {
			return this.weight - o.weight;
		}

	}
	
	private class DistComparator<T> implements Comparator<Element<T>> {

		@Override
		public int compare(Element<T> arg0, Element<T> arg1) {
			
			if (arg0.weight < arg1.weight) 
				return -1;
			if (arg0.weight > arg1.weight)
				return 1;
			else {
				if (arg0.straightLineDistSqr < arg1.straightLineDistSqr)
					return -1;
				if (arg0.straightLineDistSqr > arg1.straightLineDistSqr)
					return 1;
				return 0;
			}
		}
		
	}

	Map<Vertex, Room> vertexToRoom = new HashMap<Vertex, Room>();
	Map<Room, Vertex> roomToVertex = new HashMap<Room, Vertex>();

	Map<Room, Map<Room, List<DoorInterface>>> computedPaths = new HashMap<Room, Map<Room, List<DoorInterface>>>();


	private class Edge {
		public Vertex start;
		public Vertex end;

		public DoorInterface door;

		// TODO maybe add a list of doors corresponding to this edge?
		public Edge(Vertex start, Vertex end, DoorInterface door) {
			this.start = start;
			this.end = end;
			this.door = door;
		}

		// TODO compute an actual cost, eg. based on straight line distance /num
		// of agents in room etc
		public int getCost() {
//			Room endRoom = vertexToRoom.get(end);
//			Rectangle2D.Float bounds = endRoom.getBoundingBox();
//			int cost = (int) (bounds.height + bounds.width);
			
			return 1;
		}
	}

	private class Vertex {
		public Room room;
		public List<Edge> neighbours = new ArrayList<Edge>();

		public Map<Room, Integer> distanceTo = new HashMap<Room, Integer>();

		public Vertex(Room r) {
			this.room = r;
			distanceTo.put(r, 0);
		}
	}

	public BuildingGraph(List<Room> rooms) {
		// Build graph
		for (Room r : rooms) {
			Vertex v = createOrGetVertexForRoom(r);
			List<DoorInterface> doors = r.getDoors();

			for (DoorInterface d : doors) {
				Edge e = new Edge(v,
						createOrGetVertexForRoom((Room) d.getDestination(r)), d);
				v.neighbours.add(e);
			}
		}
		return;
	}

	private Vertex createOrGetVertexForRoom(Room r) {
		if (!roomToVertex.containsKey(r)) {
			Vertex v = new Vertex(r);
			vertexToRoom.put(v, r);
			roomToVertex.put(r, v);
			return v;
		} else {
			return roomToVertex.get(r);
		}
	}

//	private int getDistanceFromRoomToRoom(Room start, Room goal) {
//		findMinDistancesToRoom(goal);
//		return roomToVertex.get(start).distanceTo.get(goal);
//	}

	// Dijkstra's algorithm

	private void findMinDistancesToRoom(Room goal) {
		PriorityQueue<Element<Vertex>> q = new PriorityQueue<Element<Vertex>>();
		HashMap<Vertex, Element<Vertex>> elementForVertex = new HashMap<Vertex, Element<Vertex>>();

		Vertex goalVertex = roomToVertex.get(goal);

		for (Vertex v : roomToVertex.values()) {
			Element<Vertex> e = new Element<Vertex>(Integer.MAX_VALUE, v);

			if (v == goalVertex) {
				e.weight = 0;
			}

			elementForVertex.put(v, e);
			q.add(e);
		}

		Element<Vertex> min = q.poll();
		while (min != null) {
			for (Edge e : min.elem.neighbours) {
				Vertex other = e.end;
				Element<Vertex> otherElem = elementForVertex.get(other);
		
				if (otherElem.weight > min.weight + e.getCost()) {
					otherElem.weight = min.weight + e.getCost();
					other.distanceTo.put(goal, otherElem.weight);
					q.remove(otherElem);
					q.add(otherElem);
				}
			}
			min = q.poll();
		}
	}

	public List<DoorInterface> findPath(Room start, Room goal, Point2D.Float position) {
		if (computedPaths.containsKey(start)) {
			Map<Room, List<DoorInterface>> pathsForRoom = computedPaths
					.get(start);
			if (pathsForRoom.containsKey(goal)) {
				return pathsForRoom.get(goal);
			}
		}
		Vertex v = roomToVertex.get(start);
		if (!v.distanceTo.containsKey(goal)) {
			findMinDistancesToRoom(goal);
		}

		List<Element<DoorInterface>> doorsWithWeights = new ArrayList<Element<DoorInterface>>(
				v.neighbours.size());
		List<DoorInterface> doors = new ArrayList<DoorInterface>(
				v.neighbours.size());

		for (Edge e : v.neighbours) {
			try {		
				doorsWithWeights.add(new Element<DoorInterface>(e.end.distanceTo.get(goal), e.door, getStraightLineDistSqr(e.door, position)));
			} catch (NullPointerException ex) {
			}
		}

		Collections.sort(doorsWithWeights, new DistComparator<DoorInterface>());
		for (Element<DoorInterface> elem : doorsWithWeights) {
			doors.add(elem.elem);
		}

		Map<Room, List<DoorInterface>> pathsForRoom;
		if (!computedPaths.containsKey(start)) {
			pathsForRoom = new HashMap<Room, List<DoorInterface>>();
		} else {
			pathsForRoom = computedPaths.get(start);
		}
		pathsForRoom.put(goal, doors);

//		 System.out.println("<BuildingGraph> Path from " + start.getName() +
//		 " " + goal.getName());
//		 for (DoorInterface d: doors) {
//		 Door dd = (Door)d;
//		 System.out.println("<BuildingGraph> " + dd.toString());
//		 }
//		 System.out.println();
		return doors;
	}

	private float getStraightLineDistSqr(DoorInterface door, Point2D.Float position) {
		Point2D.Float d = door.getMidpoint();
		float distsqr = (float) d.distanceSq(position);
		return distsqr;
	}
}
