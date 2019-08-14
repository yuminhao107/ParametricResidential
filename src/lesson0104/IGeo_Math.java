package lesson0104;


import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.buffer.BufferOp;
import com.vividsolutions.jts.triangulate.VoronoiDiagramBuilder;

import igeo.I3DField;
import igeo.ICurve;
import igeo.IG;
import igeo.ISurface;
import igeo.IVec;
import igeo.IVecI;
import processing.core.PApplet;

public class IGeo_Math {
	public static GeometryFactory jts_gf = new GeometryFactory();

	/** General precision. */
	public static double EPSILON = 1e-8;
	public static double minAngle = Math.PI / 18.;

	/**
	 * 閿熸枻鎷烽敓鏂ゆ嫹pc_cc_cn閿熸枻鎷穋c閿熷澶勯敓鏂ゆ嫹閿熸枻鎷峰祵閿熸枻鎷烽敓锟�???
	 */
	public static double exteriorAngle(Coordinate pc, Coordinate cc, Coordinate cn) {
		double x0 = cc.x - pc.x;
		double y0 = cc.y - pc.y;
		double x1 = cn.x - cc.x;
		double y1 = cn.y - cc.y;
		return angle(x0, y0, x1, y1);
	}

	/**
	 * 閿熸枻鎷烽敓鏂ゆ�?(x0,y0)閿熸枻鎷烽敓鏂ゆ嫹閿熸枻锟�?(x1,y1)涔嬮敓鏂ゆ嫹鍕熻柂閿燂拷?
	 */
	public static double angle(double x0, double y0, double x1, double y1) {
		double l0 = Math.sqrt(x0 * x0 + y0 * y0);
		double l1 = Math.sqrt(x1 * x1 + y1 * y1);
		double l0LenX = x0 / l0;
		double l0LenY = y0 / l0;
		double l1LenX = x1 / l1;
		double l1LenY = y1 / l1;
		double dot = l0LenX * l1LenX + l0LenY * l1LenY;
		return Math.acos(dot);
	}

	public static double distance(double x0, double y0, double x1, double y1) {
		double deltaX = x0 - x1;
		double deltaY = y0 - y1;
		return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
	}

	public static Coordinate[] smooth(Coordinate[] coors) {
		return smooth(coors, minAngle);
	}

	public static double getArea(ICurve c) {
		double area = 0;
		int num = c.cpNum();
		for (int i = 0; i < num - 1; ++i) {
			area += c.cp(i + 1).cross(c.cp(i)).z();
		}

		return Math.abs(area) / 2;
	}


	public static double getArea(IVec[] pts) {
		double area = 0;
		int num = pts.length;
		for (int i = 0; i < num - 1; ++i) {
			area += pts[i + 1].cross(pts[i]).z();
		}
		area += pts[0].cross(pts[num - 1]).z();
		return Math.abs(area) / 2;
	}

	public static Polygon makeICurve2Polygon(ICurve c) {
		int num = c.cpNum() - 1;
		Coordinate[] coors = new Coordinate[num + 1];
		for (int i = 0; i < num; i++) {
			coors[i] = new Coordinate(c.cp(i).x(), c.cp(i).y());
		}
		coors[num] = coors[0];
		return jts_gf.createPolygon(coors);
	}

	public static ICurve makePolygon2ICurve(Polygon p) {
		Coordinate[] coors = p.getCoordinates();
		int num = coors.length - 1;
		IVec[] pts = new IVec[num];
		for (int i = 0; i < num; i++) {
			pts[i] = new IVec(coors[i].x, coors[i].y);
		}
		return new ICurve(pts, 1, true);
	}

	public static ICurve makeCoordinate2ICurve(Coordinate[] coors) {
		int num = coors.length;
		if (coors[0] == coors[num - 1])
			--num;
		IVec[] pts = new IVec[num];
		for (int i = 0; i < num; i++) {
			pts[i] = new IVec(coors[i].x, coors[i].y);
		}
		return new ICurve(pts, 1, true);
	}

	public static IVec[] offset(IVec[] pts, double dis) {
		int num = pts.length;
		Coordinate[] coors = new Coordinate[num + 1];
		for (int i = 0; i < num; i++) {
			coors[i] = new Coordinate(pts[i].x, pts[i].y);
		}
		coors[num] = coors[0];
		Polygon poly = jts_gf.createPolygon(coors);
		BufferOp bufOp = new BufferOp(poly);
		bufOp.setEndCapStyle(BufferOp.CAP_BUTT);
		poly = (Polygon) bufOp.getResultGeometry(-dis);
		coors = poly.getCoordinates();
		num = coors.length - 1;
		pts = new IVec[num];
		for (int i = 0; i < num; i++) {
			pts[num - 1 - i] = new IVec(coors[i].x, coors[i].y);
		}
		return pts;
	}

	public static ICurve smooth(ICurve c) {
		IVecI[] pts = c.cps();
		IVecI[] newPts = new IVecI[pts.length - 1];
		for (int i = 0; i < newPts.length; ++i) {
			newPts[i] = pts[i];
		}
		ICurve cc = new ICurve(newPts, 2, true);
		c.del();
		return cc;
	}

	/**
	 * 閿熸枻鎷烽敓鏂ゆ嫹閿熺嫛鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷疯柂璇�?敓鏂ゆ嫹閿熸枻鎷烽敓鍙槄鎷烽敓绲榥gle
	 */
	public static Coordinate[] smooth(Coordinate[] coors, double angle) {
		int leng = coors.length;
		Coordinate[] coorsCpy = coors.clone();
		if (leng < 3)
			return null;
		boolean getAngle = false;
		ArrayList<Coordinate> newCoor = new ArrayList<Coordinate>();
		while (!getAngle) {
			newCoor = new ArrayList<Coordinate>();
			getAngle = true;
			leng = coorsCpy.length;
			for (int i = 0; i < leng; ++i) {
				Coordinate currPt = coorsCpy[i];
				Coordinate prevPt = coorsCpy[(i - 1 + leng) % leng];
				Coordinate nextPt = coorsCpy[(i + 1) % leng];
				double c_angle = IGeo_Math.exteriorAngle(prevPt, currPt, nextPt);
				if (c_angle > angle) {
					getAngle = false;
					double x = prevPt.x * 0.125 + currPt.x * 0.75 + nextPt.x * 0.125;
					double y = prevPt.y * 0.125 + currPt.y * 0.75 + nextPt.y * 0.125;
					newCoor.add(new Coordinate(x, y));

					x = (currPt.x + nextPt.x) * 0.5;
					y = (currPt.y + nextPt.y) * 0.5;
					newCoor.add(new Coordinate(x, y));
				} else {
					newCoor.add(coorsCpy[i]);
				}
			}
			coorsCpy = newCoor.toArray(new Coordinate[newCoor.size()]);
		}
		return newCoor.toArray(new Coordinate[newCoor.size()]);
	}

	public static ArrayList<ICurve> printSingleCurve(ICurve c) {
		ArrayList<ICurve> res = new ArrayList<ICurve>();
		ICurve curve = c.cp();
		Geometry geo = makeICurve2Polygon(curve);
		while (geo.getNumPoints() > 3) {
			ICurve cc = makePolygon2ICurve((Polygon) geo);
			res.add(cc);
			geo = geo.buffer(-0.38);
		}
		return res;
	}

	public static float getZ(int layer) {
		float thickness = 0.2f;
		float thickness0 = 0.24f;
		return layer * thickness + thickness0;
	}
	
	public static boolean pointInsideCurve(double x, double y, ICurve c) {
		IVec p = new IVec(x, y);
		int num = c.cpNum() - 1;
		IVec p1 = c.cp(0);
		IVec p2 = c.cp(1);
		IVec p3 = c.cp(2);
		int flag = p2.dif(p1).cross(p3.dif(p1)).z() > 0 ? 1 : -1;
		for (int i = 0; i < num; ++i) {
			if (c.cp(i).dif(p).cross(c.cp(i + 1).dif(p)).z() * flag < 0.00001)
				return false;
		}
		return true;
	}
	
	public static ArrayList<ICurve> splitCurve(ICurve c, int num, double bufferDis) {
		ArrayList<ICurve> curves = new ArrayList<ICurve>();
		VoronoiDiagramBuilder vor = new VoronoiDiagramBuilder();
		Geometry site = IGeo_Math.makeICurve2Polygon(c);
		ArrayList<Coordinate> coords = new ArrayList<Coordinate>();
		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
		for (int i = 0; i < c.cpNum(); ++i) {
			IVec p = c.cp(i);
			if (p.x() < minX)
				minX = p.x();
			if (p.x() > maxX)
				maxX = p.x();
			if (p.y() < minY)
				minY = p.y();
			if (p.y() > maxY)
				maxY = p.y();
		}
		Envelope en = new Envelope(minX, maxX, minY, maxY);
		vor.setClipEnvelope(en);
		IVec[] pts = new IVec[num];
		for (int i = 0; i < num; i++) {
			double x = minX;
			double y = minY;
			while (!c.isInside2d(new IVec(x,y))) {
				x = HRand.random() * (maxX - minX) + minX;
				y = HRand.random() * (maxY - minY) + minY;
			}
			pts[i] = new IVec(x, y);
		}
		for (int i = 0; i < pts.length; i++) {
			coords.add(new Coordinate(pts[i].x(), pts[i].y()));
		}
		vor.setSites(coords);
		Geometry geo = vor.getDiagram(jts_gf);
		num = geo.getNumGeometries();
		for (int i = 0; i < num; i++) {
			Geometry gn = geo.getGeometryN(i);
			gn = gn.intersection(site);
			gn = gn.buffer(-bufferDis);
			if (gn.getNumPoints() < 3)
				continue;
			Coordinate[] cs = gn.getCoordinates();
			ICurve cc = IGeo_Math.makeCoordinate2ICurve(cs);
			cc.show();
			curves.add(cc);
		}
		return curves;
	}
	
	public static ArrayList<ICurve> splitCurve(ICurve c, IVec[] pts, double bufferDis) {
		ArrayList<ICurve> curves = new ArrayList<ICurve>();
		VoronoiDiagramBuilder vor = new VoronoiDiagramBuilder();
		Geometry site = IGeo_Math.makeICurve2Polygon(c);
		ArrayList<Coordinate> coords = new ArrayList<Coordinate>();
		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
		for (int i = 0; i < c.cpNum(); ++i) {
			IVec p = c.cp(i);
			if (p.x() < minX)
				minX = p.x();
			if (p.x() > maxX)
				maxX = p.x();
			if (p.y() < minY)
				minY = p.y();
			if (p.y() > maxY)
				maxY = p.y();
		}
		Envelope en = new Envelope(minX, maxX, minY, maxY);
		vor.setClipEnvelope(en);
		for (int i = 0; i < pts.length; i++) {
			coords.add(new Coordinate(pts[i].x(), pts[i].y()));
		}
		vor.setSites(coords);
		Geometry geo = vor.getDiagram(jts_gf);
		int num = geo.getNumGeometries();
		for (int i = 0; i < num; i++) {
			Geometry gn = geo.getGeometryN(i);
			gn = gn.intersection(site);
			gn = gn.buffer(-bufferDis);
			if (gn.getNumPoints() < 3)
				continue;
			Coordinate[] cs = gn.getCoordinates();
			ICurve cc = IGeo_Math.makeCoordinate2ICurve(cs);
			curves.add(cc);
		}
		return curves;
	}
	
	public static Geometry Voronoi(ICurve c, int num) {
		VoronoiDiagramBuilder vor = new VoronoiDiagramBuilder();
		Geometry site = IGeo_Math.makeICurve2Polygon(c);
		ArrayList<Coordinate> coords = new ArrayList<Coordinate>();
		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
		for (int i = 0; i < c.cpNum(); ++i) {
			IVec p = c.cp(i);
			if (p.x() < minX)
				minX = p.x();
			if (p.x() > maxX)
				maxX = p.x();
			if (p.y() < minY)
				minY = p.y();
			if (p.y() > maxY)
				maxY = p.y();
		}
		Envelope en = new Envelope(minX, maxX, minY, maxY);
		vor.setClipEnvelope(en);
		IVec[] pts = new IVec[num];
		for (int i = 0; i < num; i++) {
			double x = minX;
			double y = minY;
			while (!pointInsideCurve(x, y, c)) {
				x = HRand.random() * (maxX - minX) + minX;
				y = HRand.random() * (maxY - minY) + minY;
			}
			pts[i] = new IVec(x, y);
		}
		for (int i = 0; i < pts.length; i++) {
			coords.add(new Coordinate(pts[i].x(), pts[i].y()));
		}
		vor.setSites(coords);
		Geometry geo = vor.getDiagram(jts_gf);
		geo=geo.intersection(site);
		return geo;
		
	}
 
	public static Geometry Voronoi(ICurve c, IVec[] pts) {
		VoronoiDiagramBuilder vor = new VoronoiDiagramBuilder();
		Geometry site = IGeo_Math.makeICurve2Polygon(c);
		ArrayList<Coordinate> coords = new ArrayList<Coordinate>();
		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
		for (int i = 0; i < c.cpNum(); ++i) {
			IVec p = c.cp(i);
			if (p.x() < minX)
				minX = p.x();
			if (p.x() > maxX)
				maxX = p.x();
			if (p.y() < minY)
				minY = p.y();
			if (p.y() > maxY)
				maxY = p.y();
		}
		Envelope en = new Envelope(minX, maxX, minY, maxY);
		vor.setClipEnvelope(en);
		for (int i = 0; i < pts.length; i++) {
			coords.add(new Coordinate(pts[i].x(), pts[i].y()));
		}
		vor.setSites(coords);
		Geometry geo = vor.getDiagram(jts_gf);
		geo = geo.intersection(site);
		return geo;

	}

	public static Geometry centerVoronoi(ICurve c, int num, int step) {

		ArrayList<ICurve> sites = IGeo_Math.splitCurve(c, num, 0);
		for (int i = 0; i < step; i++) {
			IVec[] pts = new IVec[num];
			for (int j = 0; j < num; j++) {
				pts[j] = sites.get(j).center();
				sites.get(j).del();
			}
			if (i == step - 1)
				return Voronoi(c, pts);
			sites = IGeo_Math.splitCurve(c, pts, 0);
		}
		return null;
	}

	public static Geometry centerVoronoi(ICurve c, int num, int step, ICurve[] curves) {

		ArrayList<ICurve> sites = IGeo_Math.splitCurve(c, num, 0);
		for (int i = 0; i < step; i++) {
			IVec[] pts = new IVec[num];
			for (int j = 0; j < num; j++) {
				pts[j] = sites.get(j).center();
				sites.get(j).del();
			}
			if (i == step - 1)
				return Voronoi(c, pts);
			for (int j = 0; j < num; j++) {
				double min = Double.MAX_VALUE;
				int id = 0;
				for (int k = 0; k < curves.length; k++) {
					double dist = curves[k].dist(pts[j]);
					if (dist < min) {
						min = dist;
						id = k;
					}
				}
				IVec target = curves[id].closePt(pts[j]);
				pts[j].add(target.dif(pts[j]).len(5000));
			}
			sites = IGeo_Math.splitCurve(c, pts, 0);
		}
		return null;
	}

	public static IVec crossPoint(IVec p1, IVec dir1, IVec p2, IVec dir2) {
		double x = (dir1.cross(p1).z() - dir1.cross(p2).z()) / dir1.cross(dir2).z();
		return p2.sum(dir2.cp().mul(x));
	}

	public static void box(IVec[] pts, double h) {
		IG.extrude(pts, 1,true,h);
		new ISurface(pts).mv(0, 0, h);
	}
	
	public static void box(IVec[] pts,double h,double z,String wall,String roof){
		IVec[] pts2=new IVec[pts.length];
		for (int i=0;i<pts.length;i++) pts2[i]=pts[i].cp();
		ISurface s1=IG.extrude(pts, 1,true,new IVec(0,0,h));
		s1.mv(0, 0, z);
		s1.layer(wall).clr(0,0,1.);
		ISurface s2=new ISurface(pts2);
		s2.mv(0,0,h+z);
		s2.layer(roof).clr(0,0,1.);
	}
	
	public static IVec centerOfPts(IVec[] pts){
		double sum=0;
		IVec res=new IVec();
		for (int i=0;i<pts.length;i++){
			IVec p1=pts[i];
			IVec p2=pts[(i+1)%pts.length];
			double dist=p1.dist(p2);
			sum+=dist;
			res.add(p1.mid(p2).mul(dist));
		}
		return res.mul(1./sum);
		
	}
	
	public static boolean crossLine(IVec p1,IVec p2,IVec p3,IVec p4){
		double x1=p2.dif(p1).cross(p3.dif(p1)).z();
		double x2=p2.dif(p1).cross(p4.dif(p1)).z();
		x1=x1*x2;
		double x3=p4.dif(p3).cross(p1.dif(p3)).z();
		double x4=p4.dif(p3).cross(p2.dif(p3)).z();
		x4=x3*x4;
		
		if (x1<0 && x4<0 ) {
//			System.out.println("false");
			return true;
			
		}
		else{
			return false;
		}
	}

}
