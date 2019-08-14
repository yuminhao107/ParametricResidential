package lesson0104;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.operation.buffer.BufferOp;

import igeo.I3DField;
import igeo.ICurve;
import igeo.IG;
import igeo.IVec;

public class SitePlan {
	HalfEdgeGeo geo;
	ArrayList<I3DField> fields = new ArrayList<I3DField>();
	double randomR = 500;
	ArrayList<ICurve> rivers = new ArrayList<ICurve>();
	ArrayList<Node> nodes;
	ICurve green;
	double streetWidth = Option.streetWidth;
	int id = 0;

	ArrayList<BuildingPlan> buildingPlans = new ArrayList<BuildingPlan>();

	int changeNum = 0;

	private Geometry union;

	public SitePlan(HalfEdgeGeo geo, double r) {
		this.geo = geo;
		geo.show();
		geo.layer("BuildingSite");
		this.randomR = r;
		for (Face face : geo.faces) {
			// face.area = IGeo_Math.getArea(face.pts());
			face.area = Option.site_Single_Area * 1000000;
		}
	}

	public void drawRoad() {
		GeometryFactory gf = new GeometryFactory();
		ArrayList<Geometry> geoList = new ArrayList<Geometry>();
		for (Edge edge : geo.edges) {
			if (edge.isRoad) {
				Coordinate[] coors = new Coordinate[2];
				coors[0] = new Coordinate(edge.p1.pos().x(), edge.p1.pos().y());
				coors[1] = new Coordinate(edge.p2.pos().x(), edge.p2.pos().y());
				geoList.add(gf.createLineString(coors));
//				edge.isRoad = false;
//				if (edge.pair != null)
//					edge.pair.isRoad = false;
			}
		}
		Geometry[] geoArray = geoList.toArray(new Geometry[0]);
		GeometryCollection geoCollection = gf.createGeometryCollection(geoArray);
		// BufferOp bufOp=new BufferOp(geoCollection);
		// bufOp.setEndCapStyle(BufferOp.CAP_SQUARE);
		// Geometry union = bufOp.getResultGeometry(streetWidth/2);
		union = geoCollection.buffer(streetWidth / 2);

		int num = union.getNumGeometries();
		System.out.println(num);
		for (int i = 0; i < num; i++) {
			Geometry c_g = union.getGeometryN(i);
			if (c_g.getNumPoints() < 3)
				continue;
			Polygon pp = (Polygon) c_g;
			Coordinate[] cs = pp.getExteriorRing().getCoordinates();
			ICurve c = IGeo_Math.makeCoordinate2ICurve(cs);
			c.clr(1., 0, 0);
			c.layer(Option.layer_Road);
			int num2 = pp.getNumInteriorRing();
			System.out.println(num2);
			for (int j = 0; j < num2; j++) {
				Coordinate[] cs2 = pp.getInteriorRingN(j).getCoordinates();
				ICurve cc = IGeo_Math.makeCoordinate2ICurve(cs2);
				cc.clr(1., 0, 0);
				cc.layer(Option.layer_Road);
			}
		}
	}

	public void drawWalk() {
		GeometryFactory gf = new GeometryFactory();
		ArrayList<Geometry> geoList = new ArrayList<Geometry>();
		for (Edge edge : geo.edges) {
			if (edge.pair != null){
				if (!edge.isRoad)
					if (edge.face.isBuilding || edge.pair.face.isBuilding){
						Coordinate[] coors = new Coordinate[2];
						coors[0] = new Coordinate(edge.p1.pos().x(), edge.p1.pos().y());
						coors[1] = new Coordinate(edge.p2.pos().x(), edge.p2.pos().y());
						geoList.add(gf.createLineString(coors));
					}
			}else {
				if (edge.face.isBuilding){
					Coordinate[] coors = new Coordinate[2];
					coors[0] = new Coordinate(edge.p1.pos().x(), edge.p1.pos().y());
					coors[1] = new Coordinate(edge.p2.pos().x(), edge.p2.pos().y());
					geoList.add(gf.createLineString(coors));
				}
			}
		}
		Geometry[] geoArray = geoList.toArray(new Geometry[0]);
		GeometryCollection geoCollection = gf.createGeometryCollection(geoArray);
		Geometry union2 = geoCollection.buffer(Option.walk_Width / 2);
        union2=union2.difference(union);
		int num = union2.getNumGeometries();
		System.out.println(num);
		for (int i = 0; i < num; i++) {
			Geometry c_g = union2.getGeometryN(i);
			if (c_g.getNumPoints() < 3)
				continue;
			Polygon pp = (Polygon) c_g;
			Coordinate[] cs = pp.getExteriorRing().getCoordinates();
			ICurve c = IGeo_Math.makeCoordinate2ICurve(cs);
			c.clr(0, 1., 0);
			c.layer(Option.layer_Road+2);
			int num2 = pp.getNumInteriorRing();
			System.out.println(num2);
			for (int j = 0; j < num2; j++) {
				Coordinate[] cs2 = pp.getInteriorRingN(j).getCoordinates();
				ICurve cc = IGeo_Math.makeCoordinate2ICurve(cs2);
				cc.clr(0, 1., 0);
				cc.layer(Option.layer_Road+2);
			}
		}
	}

	public void addRiver(ICurve river) {
		this.rivers.add(river);
	}

	public void addFiedld(I3DField field) {
		fields.add(field);
	}

	public void addBuil() {
		for (Face face : geo.faces) {
			ICurve curve = new ICurve(IGeo_Math.offset(face.pts(), 1), 1, true);
			IG.extrude(curve, 6);
		}
	}

	public void generateBuilding() {
		this.checkWhichIsBuilding();
		for (int i = 0; i < Option.thread_Num; i++) {
			this.generateNextBuilding();
		}
	}

	public synchronized void generateNextBuilding() {
		int num = geo.faces.size();
		if (id >= num)
			return;
		while ((!geo.faces.get(id).isBuilding)) {
			++id;
			if (id >= num)
				break;
		}
		if (id < num) {
			Face face = geo.faces.get(id);
			buildingPlans.add(new BuildingPlan(face, this));
			System.out.println("Building num: " + id + " of " + num);
			++id;
		}
	}

	public void drawBuildings() {
		for (BuildingPlan plan : buildingPlans)
			if (plan.tryNum < Option.building_Try_Num)
				plan.drawUpstair();
	}

	boolean crossRiver(ICurve c) {
		for (ICurve river : rivers) {
			int num = river.cpNum();
			for (int i = 0; i < num; i++) {
				if (c.isInside2d(river.cp(i)))
					return true;
			}
		}
		return false;
	}

	public void tryChange() {
		if (nodes == null) {
//			System.out.println("nodes is null");
			nodes = new ArrayList<Node>();
			for (Node node : geo.nodes) {
				if (!node.isOutside())
					nodes.add(node);
			}
		}
		int num = nodes.size();
		int id = (int) (HRand.random() * num);
		Node node = nodes.get(id);
		double cost = this.nodeNeighbourCost(node);
		IVec pos = node.pos().cp();
		double x = randomR;
		double y = randomR;
		while (x * x + y * y > randomR * randomR) {
			x = (HRand.random() - .5) * 2 * randomR;
			y = (HRand.random() - .5) * 2 * randomR;
		}
		node.pos.add(new IVec(x, y));
		double nextCost = this.nodeNeighbourCost(node);
		if (cost > nextCost) {
			node.pos.set(pos);
			changeNum++;
		} else {
			changeNum = 0;
		}
	}

	private double nodeNeighbourCost(Node p) {
		double cost = 1;
		for (Face face : p.faces()) {
			IVec[] envolope = face.envolope();
			IVec[] pts = face.pts();
			cost *= Math.pow(angelCost(pts), Option.site_Angle_Importace);
			cost *= Math.pow(shapeCost(envolope), Option.site_Shape_Importace);
			cost *= Math.pow(directionCost(envolope), Option.site_Direction_Importace);
			cost *= Math.pow(areaCost(face), Option.site_Area_Importace);
			cost *= Math.pow(rectCost(pts, envolope), Option.site_Rect_Importace);
			cost *= Math.pow(roadCost(face), Option.site_Road_Importace);
		}
		return cost;
	}

	private void checkWhichIsBuilding() {
		for (Face face : geo.faces) {
			IVec[] envolope = face.envolope();
			IVec[] pts = face.pts();
			double cost = areaCost(face);
			if (cost < Option.building_area_Tol) {
				face.isBuilding = false;
				continue;
			}
			cost = shapeCost(envolope);
			if (cost < Option.building_Shape_Tol) {
				face.isBuilding = false;
				continue;
			}
			face.isBuilding = true;
		}
	}

	private int checkBuildingIsConnected() {
		int num = 0;
		for (Face face : geo.faces) {
			Edge edge = face.first;
			face.connect = false;
			do {
				if (edge.p1.isRoad) {
					face.connect = true;
					break;
				}
				edge = edge.next;
			} while (edge != face.first);
			if (!face.connect)
				++num;

		}
		return num;
	}

	public IVec getForce(IVec p) {
		IVec res = new IVec();
		for (I3DField field : fields) {
			res.add(field.get(p));
		}
		return res;
	}

	public double rectCost(IVec[] pts, IVec[] envolope) {

		double res = IGeo_Math.getArea(pts) / IGeo_Math.getArea(envolope);

		return res;
	}

	public double roadCost(Face face) {
		Edge edge = face.first;
		double res = 0;
		int count = 0;
		do {
			if (edge.isRoad && edge.next.isRoad) {
				++count;
				IVec p1 = edge.p1.pos();
				IVec p2 = edge.p2.pos();
				IVec p3 = edge.next.p2.pos();
				double ang = p1.dif(p2).angle(p3.dif(p2));
				res += ang / Math.PI;
			}
			edge = edge.next;
		} while (edge != face.first);
		if (count == 0)
			return 1;
		return res / count;
	}

	public double angelCost(IVec[] pts) {
		double sum = 0;
		int num = pts.length;
		IVec z = new IVec(0, 0, 1);
		for (int i = 0; i < num; i++) {
			double ang = pts[(i + 1) % num].dif(pts[i]).angle(pts[(i - 1 + num) % num].dif(pts[i]), z);
			// if (ang<0) ang=Math.PI*2+ang;
			if (ang < 0) {
				sum += 0.01;
			} else {
				sum += 1 - Math.abs(Math.min(Math.sin(ang), -Math.cos(ang)));
			}
		}
		return sum / num;
	}

	public double shapeCost(IVec[] pts) {
		double w = pts[0].dist(pts[2]);
		double h = pts[1].dist(pts[3]);
		if (w > h)
			return h / w;
		return w / h;
	}

	public double directionCost(IVec[] pts) {
		IVec center = new IVec();
		for (int i = 0; i < pts.length; i++) {
			center.add(pts[i]);
		}
		center.mul(1. / pts.length);
		IVec force = getForce(center);
		// if (force.len()<0.1) return 1;
		double min = 10;
		for (int i = 0; i < pts.length; i++) {
			double ang = pts[(i + 1) % pts.length].dif(pts[i]).angle(force);
			if (ang < min)
				min = ang;
		}
		return 1 - min / (Math.PI / 2);
	}

	public double areaCost(Face face) {
		double now = IGeo_Math.getArea(face.pts());
		double area = face.area;
		if (now > area)
			return area / now;
		else
			return now / area;
	}

}
