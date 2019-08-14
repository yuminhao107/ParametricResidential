package lesson0104;

import java.util.ArrayList;
import java.util.Random;

import com.vividsolutions.jts.geom.Geometry;

import igeo.*;

public class BuildingPlan {
	Face face;
	HalfEdgeGeo geo;
	double randomR = 100;
	double area;
	double streetWidth = Option.building_Offset * 2;
	HServer server;
	SitePlan sitePlan;

	int suitNum;
	int roomNum = 4;

	int tryNum = 0;
	int changeNum = 0;

	ICurve bundary;

	private Random rand;

	ArrayList<HTL> htls = new ArrayList<HTL>();
	ArrayList<HCircle> points = new ArrayList<HCircle>();
	IPoly poly;

	public BuildingPlan(Face face, SitePlan sitePlan) {
		this.face = face;
		this.sitePlan = sitePlan;
		rand = new Random(Option.random_Seed_Building);
		this.initialize2();
	}

	private double randomDouble() {
		return rand.nextDouble();
	}

	private IVec randomIVec() {
		double x = (randomDouble() - .5) * 2000;
		double y = (randomDouble() - .5) * 2000;
		return new IVec(x, y);
	}

	void initialize() {
		ICurve curve = new ICurve(IGeo_Math.offset(face.pts(), streetWidth / 2), 1, true);
		area = IGeo_Math.getArea(curve);
		System.out.println("BuildingSite area is " + area);
		suitNum = (int) (area / 60000000);
		System.out.println(suitNum);

		suitNum = 3;
		Geometry jtsGeo = IGeo_Math.centerVoronoi(curve, suitNum * roomNum + 3, 5);
		geo = new HalfEdgeGeo(jtsGeo);
		curve.del();
		this.generateSuit();

	}

	void initialize2() {
		server = new HServer(this);
		IVec[] pts = IGeo_Math.offset(face.pts(), streetWidth / 2);
		if (bundary == null) {
			IVec[] pts2 = IGeo_Math.offset(face.pts(), Option.streetWidth / 2);
			bundary = new ICurve(pts2, 1, true);
			bundary.layer(Option.layer_Useless);
			bundary.hide();
			// bundary.hide();
		}
		// ICurve curve = new ICurve(IGeo_Math.offset(face.pts(), streetWidth /
		// 2), 1, true);
		area = IGeo_Math.getArea(pts);

		System.out.println("BuildingSite area is " + (int) (area / 1000000));
		suitNum = (int) (area / (Option.building_Single_Suit_Area * 1000000));
		System.out.println("Suit Num is " + suitNum);

		// suitNum = 4;
		if (suitNum > Option.building_Suit_Max_Num)
			suitNum = Option.building_Suit_Max_Num;
		if (suitNum < Option.building_Suit_Min_Num)
			suitNum = Option.building_Suit_Min_Num;
		poly = new IPoly(server, pts);

		IVec traffic = face.entrance();
		int id = 0;
		double min = Double.MAX_VALUE;
		for (int i = 0; i < pts.length; i++) {
			double dist = pts[i].dif(traffic).len();
			if (dist < min) {
				min = dist;
				id = i;
			}
		}
		IVec p = pts[id];
		IVec pp = pts[(id - 1 + pts.length) % pts.length];
		IVec pn = pts[(id + 1) % pts.length];
		double length = Math.sqrt(RoomType.entrance.area) / 2;
		IVec entranceP = pp.dif(p).unit().sum(pn.dif(p).unit()).len(length).add(p);
		IVec center = face.center();
		HCircle entrance = new HCircle(server, entranceP, RoomType.entrance, 0);
		entrance.fix();
		HCircle stair = new HCircle(server, randomIVec().add(center), RoomType.stair, 0);
		HCircle corridor = new HCircle(server, randomIVec().add(center), RoomType.corridor, 0);
		htls.add(new HTL(server, entrance, stair));
		htls.add(new HTL(server, stair, corridor));
		points.add(entrance);
		points.add(corridor);
		points.add(stair);
		for (int i = 1; i <= suitNum; i++) {
			HCircle living = new HCircle(server, randomIVec().add(center), RoomType.livingroom, i);
			HCircle bed = new HCircle(server, randomIVec().add(center), RoomType.bedroom, i);
			// HCircle bed2 = new HCircle(IG.randomIVec().add(center),
			// RoomType.bedroom, i);
			HCircle bath = new HCircle(server, randomIVec().add(center), RoomType.bathroom, i);
			HCircle dining = new HCircle(server, randomIVec().add(center), RoomType.diningroom, i);
			// HCircle kit = new HCircle(IG.randomIVec().add(center),
			// RoomType.kitchen, i);
			points.add(living);
			points.add(bed);
			points.add(bath);
			points.add(dining);
			// points.add(bed2);
			// points.add(kit);

			htls.add(new HTL(server, living, corridor));
			htls.add(new HTL(server, bed, living));
			htls.add(new HTL(server, dining, living));
			htls.add(new HTL(server, bath, living));
			// htls.add(new HTL(living, kit));
			// htls.add(new HTL(bed2, living));
		}

		double sum = 0;
		for (HCircle point : points)
			sum += point.type.area;
		for (HCircle point : points) {
			point.targetArea = area / sum * point.type.area;
			point.radius = Math.sqrt(point.targetArea / Math.PI);
		}

		server.start();

	}

	public boolean isStill() {
		for (HCircle p : points) {
			if (p.vel().len() > 600) {
				return false;
			}
		}
		return true;
	}

	public void generateHalfEdgeGeo() {
		ICurve curve = new ICurve(IGeo_Math.offset(face.pts(), streetWidth / 2), 1, true);
		IVec[] pts = new IVec[points.size()];
		for (int i = 0; i < points.size(); i++) {
			pts[i] = points.get(i).pos().cp();
		}
		Geometry jtsGeo = IGeo_Math.Voronoi(curve, pts);
		geo = new HalfEdgeGeo(jtsGeo);
		for (Face face : geo.faces) {
			ICurve c = new ICurve(face.pts(), 1, true);
			for (HCircle p : points) {
				if (c.isInside2d(p.pos())) {
					face.type = p.type;
					face.suitId = p.suitId;
					face.targetArea = p.targetArea;
					break;
				}
			}
			c.layer(Option.layer_Useless);
			c.hide();
		}
		server.stop();
		for (HTL htl : htls)
			htl.del();
		for (HCircle p : points)
			p.del();
		htls.clear();
		points.clear();
		curve.layer(Option.layer_Useless);
		curve.hide();

		// System.out.println("haha"+geo.faces.size());

	}

	public boolean htlIsNoCross() {
		for (int i = 0; i < htls.size(); i++) {
			HTL line1 = htls.get(i);
			for (int j = i + 1; j < htls.size(); j++) {
				HTL line2 = htls.get(j);
				if (line1.pt1 == line2.pt1 || line1.pt1 == line2.pt2 || line1.pt2 == line2.pt1
						|| line1.pt2 == line2.pt2)
					continue;
				if (IGeo_Math.crossLine(line1.pt1.pos(), line1.pt2.pos(), line2.pt1.pos(), line2.pt2.pos()))
					return false;
			}
		}
		return true;
	}

	public void checkColor() {
		for (Edge edge : geo.edges) {

			if (edge.pair == null) {
				edge.curve.layer("Outside Wall");
			} else {
				edge.curve.layer("Inside Wall");
			}
			if (edge.pair == null) {
				edge.curve.clr(0.);
				continue;
			}
			if (edge.face.suitId == edge.pair.face.suitId) {
				edge.curve.clr(0.8);
			} else {
				edge.curve.clr(0.);
			}
			// if (edge.isRoad) edge.curve.clr(1.,0,0);
		}
	}

	public void drawUpstair() {
		ArrayList<IVec[]> layers=new ArrayList<IVec[]>();
		
		double height = Option.building_Height;
		Face entrance=null;
		for (Face face : geo.faces) {
			if (face.type.equals(RoomType.entrance)) {
//				face.isVoid = true;
				entrance=face;
				break;
			}
		}
		Face stair = null;
		for (Face face : geo.faces) {
			if (face.type.equals(RoomType.stair)) {
//				face.isVoid = true;
				stair = face;
				break;
			}
		}
//		IGeo_Math.box(geo.outsidePts(), height, 0, Option.layer_Wall_Outside,Option.layer_Wall_Floor);
		layers.add(geo.outsidePts());
		new ISurface(geo.outsidePts()).layer(Option.layer_Wall_Floor).mv(0,0,height);
		entrance.isVoid=true;
		stair.isVoid=true;
		drawWalls(0);
		for (int i = 1; i < Option.voidNum[suitNum][0]; ++i) {
			stair.isVoid = false;
			for (int j = 0; j < Option.voidNum[suitNum][i]; ++j)
				findVoidRoom();
			layers.add(geo.outsidePts());
			stair.isVoid =true;
//			IGeo_Math.box(geo.outsidePts(), height, height * i, Option.layer_Wall_Outside,Option.layer_Wall_Floor);
			drawWalls(i*height);
			new ISurface(geo.outsidePts()).layer(Option.layer_Wall_Floor).mv(0,0,(i+1)*height);
		}
		new ICurve(geo.roomPts(0, RoomType.entrance), 1, true).layer(Option.layer_Area_Entrance).hide();
		new ICurve(geo.roomPts(0, RoomType.stair), 1, true).layer(Option.layer_Area_Stair).hide();
		IGeo_Math.box(geo.roomPts(0, RoomType.stair), height*(Option.voidNum[suitNum][0]-0.2), 0, Option.layer_Area_Stair,Option.layer_Area_Stair);
		
		
		if (Option.print_Building_Card){
			IVec center=null;
	    	for (int i=layers.size()-1;i>=0;i--){
	    		IVec[] pts=layers.get(i); 
	    		if (center==null) center=IGeo_Math.centerOfPts(pts);
	    		IVec print=Print.printOrigin();
	    		new IText(face.num+" ",Print.txtSize,print).layer(Option.layer_Building_Card);
	    		new IText(face.num+" ",Print.txtSize,center).layer(Option.layer_Building_Card);
	    		for (IVec v:pts) v.add(print.dif(center));
	    		new ICurve(pts,1,true).layer(Option.layer_Building_Card);
	    	}
	    }
	}
	
	private void drawWalls(double z){
		Edge first=null;
		for (Edge edge:geo.edges){
			if (edge.pair==null){
				if (!edge.face.isVoid){
					first=edge;
//					drawWall(first,z,true);
					break;
				}
			}else{
				if (!edge.face.isVoid && edge.pair.face.isVoid){
					first=edge;
//					drawWall(first,z,false);
					break;
				}
			}
		}
		Edge edge=first;
		do{
			for(Edge e:edge.p2.edges){
				if (e.pair==null){
					if (!e.face.isVoid){
					edge=e;
					drawWall(edge,z,true);
					break;
					}
				}else{
					if (!e.face.isVoid && e.pair.face.isVoid){
						edge=e;
						drawWall(edge,z,false);
						break;
					}
				}
			}
		}while(edge!=first);
	}
	
	private void drawWall(Edge edge,double z,boolean isOutside){
		double height=Option.building_Height;
		double height2=Option.building_Height_NvErQiang;
		IVec[] pts=new IVec[4];
		pts[0]=edge.p1.pos().cp().add(0,0,z);
		pts[1]=edge.p2.pos().cp().add(0,0,z);
		double width=pts[0].dist(pts[1]);
		pts[2]=pts[1].sum(0, 0, height);
		pts[3]=pts[0].sum(0, 0, height);
//		if (!isOutside && randomDouble()>0.7) isOutside=true;
		if (isOutside){
		    ISurface surface=new ISurface(pts).layer(Option.layer_Wall_Outside);
		    if (width<Option.building_Window_Width*3 || randomDouble()>0.5) return;
		    double windowSize=randomDouble()*500+600;
		    double x=randomDouble()*(1-windowSize/width-0.2)+0.1;
		    double y=randomDouble()*(1-700./height-windowSize/height)+500./height;
		    double w=windowSize/width;
		    double h=windowSize/height;
		    ICurve curve=new ICurve(new IVec[]{new IVec(x,y),new IVec(x+w,y),new IVec(x+w,y+h),new IVec(x,y+h)},1,true);
		    surface.addInnerTrimLoop(curve);
		    curve.layer(Option.layer_Useless);
		    curve.hide();
		    IVec pts2[]=new IVec[4];
		    pts2[0]=pts[1].dif(pts[0]).mul(x).add(0,0,height*y).add(pts[0]);
		    pts2[1]=pts[1].dif(pts[0]).mul(x+w).add(0,0,height*y).add(pts[0]);
		    pts2[2]=pts[1].dif(pts[0]).mul(x+w).add(0,0,height*(y+h)).add(pts[0]);
		    pts2[3]=pts[1].dif(pts[0]).mul(x).add(0,0,height*(y+h)).add(pts[0]);
		    ISurface window=new ISurface(pts2);
		    window.clr(0,0,1.);
		    window.layer(Option.layer_Wall_Window);
		}else{
			if (width<Option.building_Window_Width*3 || randomDouble()>0.7){
				ISurface surface=new ISurface(pts);
				surface.layer(Option.layer_Wall_Outside_White);
				return;
			}
			IVec[] wPts=new IVec[4];
			wPts[0]=pts[0].sum(pts[1].dif(pts[0]).len(Option.building_Window_Width));
			wPts[1]=pts[1].sum(pts[0].dif(pts[1]).len(Option.building_Window_Width));
			wPts[2]=wPts[1].sum(0,0,Option.building_Window_Height);
			wPts[3]=wPts[0].sum(0,0,Option.building_Window_Height);
			IVec[] aPts=new IVec[8];
			aPts[0]=pts[0];
			for (int i=1;i<5;i++) aPts[i]=wPts[(5-i)%4];
			for (int i=5;i<8;i++) aPts[i]=pts[i-4];
			new ISurface(aPts).layer(Option.layer_Wall_Outside_White);
			new ISurface(wPts).layer(Option.layer_Wall_Window).clr(0,0,1.);
		}
		
	}

	private void findVoidRoom() {
		Face select = null;
		int min = Integer.MAX_VALUE;
		for (Face face : geo.faces) {
			if (face.isVoid)
				continue;
			if (face.type.equals(RoomType.stair))
				continue;
			boolean near = false;
			int num = 0;
			Edge edge = face.first;
			do {
				if (edge.pair != null) {
					if (!edge.pair.face.isVoid) {
						++num;
					} else {
						near = true;
					}
				}
				edge = edge.next;
			} while (edge != face.first);
			// if (!near) continue;
			if (num < min) {
				min = num;
				select = face;
			}
		}
		select.isVoid = true;
	}

	public void change1() {
		int num = geo.nodes.size();
		int id = (int) (randomDouble() * num);
		Node node = geo.nodes.get(id);

		double x = randomR;
		double y = randomR;
		while (x * x + y * y > randomR * randomR) {
			x = (randomDouble() - .5) * 2 * randomR;
			y = (randomDouble() - .5) * 2 * randomR;
		}
		IVec dp = new IVec(x, y);

		if (!Option.building_Change_Outside_Point)
			if (node.isOutside()) {
				if (node.edges.size() < 2)
					return;
				Edge out = null;
				for (Edge edge : node.edges)
					if (edge.pair == null)
						out = edge;
				double len = x = (randomDouble() - .5) * 2 * randomR;
				dp = out.p2.pos().dif(node.pos()).len(len);
			}
		if (node.isOutside()) {
			if (!bundary.isInside2d(node.pos().sum(dp)))
				return;
		}
		double value = this.roomValue();
		IVec pos = node.pos().cp();
		node.pos.add(dp);
		double nextValue = this.roomValue();
		if (value > nextValue) {
			node.pos.set(pos);
			changeNum++;
		} else {
			changeNum = 0;
		}
	}

	public void change2() {
		int num = geo.faces.size();
		int id1 = (int) (randomDouble() * num);
		while (geo.faces.get(id1).type.equals(RoomType.entrance))
			id1 = (int) (randomDouble() * num);
		int id2 = id1;
		while (id2 == id1 || geo.faces.get(id2).type.equals(RoomType.entrance))
			id2 = (int) (randomDouble() * num);
		double value = this.roomValue();
		swapRoom(geo.faces.get(id1), geo.faces.get(id2));
		double nextValue = this.roomValue();
		if (value > nextValue)
			swapRoom(geo.faces.get(id1), geo.faces.get(id2));

	}

	private void swapRoom(Face f1, Face f2) {
		RoomType tem = f1.type;
		f1.type = f2.type;
		f2.type = tem;
		int t = f1.suitId;
		f1.suitId = f2.suitId;
		f2.suitId = t;
	}

	private double roomValue() {
		double cost = 1;
		for (Face face : geo.faces) {
			// geometry value
			IVec[] envolope = face.envolope();
			IVec[] pts = face.pts();
			cost *= Math.pow(angelCost(pts), Option.building_Angle_Importace); // 减少锐角
			cost *= Math.pow(shapeCost(envolope), Option.building_Shape_Importace); // 长宽比接近1
			cost *= Math.pow(areaCost(face), Option.building_Area_Importace); // 面积符合要求
			cost *= Math.pow(rectCost(pts, envolope), Option.building_Rect_Importace);// 接近矩形
			// connection value;
			// cost*=Math.pow(connectionValue(face),1.5);
			cost *= Math.pow(doorCost(face), Option.building_Door_Importace);

		}
		return cost;
	}

	public boolean connectionIsOK() {
		boolean found = false;
		for (Face face : geo.faces) {
			if (this.connectionValue(face) < 1)
				found = true;
		}
		if (found)
			return false;
		return true;
	}

	private double connectionValue(Face face) {
		RoomType[] rooms = face.type.rooms;
		if (rooms.length == 0)
			return 1;
		Edge edge = face.first;
		do {
			if (edge.pair != null)
				if (edge.pair.face.suitId == face.suitId || edge.pair.face.suitId == 0) {
					boolean found = false;
					for (int i = 0; i < rooms.length; ++i) {
						if (rooms[i].equals(edge.pair.face.type)) {
							found = true;
							edge.isRoad = true;
							break;
						}
					}
					if (found) {
						return 1;
					}
				}
			edge = edge.next;
		} while (edge != face.first);
		return face.type.connectionImportance;
	}

	private double doorCost(Face face) {
		Edge edge = face.first;
		do {
			if (edge.isRoad) {
				double dist = edge.dist2();
				double target = Option.building_Door_Min_Dist;
				if (dist < target)
					return dist / target;
			}
			edge = edge.next;
		} while (edge != face.first);
		return 1;
	}

	private double areaCost(Face face) {
		double now = IGeo_Math.getArea(face.pts());
		face.area = now / 1000000;
		double area = face.targetArea;
		if (area == 0)
			return 1;
		if (now > area)
			return area / now;
		else
			return now / area;
	}

	private double shapeCost(IVec[] pts) {
		double w = pts[0].dist(pts[2]);
		double h = pts[1].dist(pts[3]);
		if (w > h)
			return h / w;
		return w / h;
	}

	private double angelCost(IVec[] pts) {
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

	private double rectCost(IVec[] pts, IVec[] envolope) {

		double res = IGeo_Math.getArea(pts) / IGeo_Math.getArea(envolope);

		return res;
	}

	void generateSuit() {
		IVec road = face.entrance();
		double min = Double.MAX_VALUE;
		Face select = null;
		for (Face face : geo.faces) {
			double dist = face.center().dif(road).len();
			if (dist < min) {
				min = dist;
				select = face;
			}
		}
		select.type = RoomType.entrance;
		select.suitId = 0;

		select = findFace(0, true);
		select.type = RoomType.stair;
		select.suitId = 0;

		select = findFace(0, true);
		select.type = RoomType.corridor;
		select.suitId = 0;

		for (int i = 1; i <= suitNum; i++) {
			select = findFace(0, false);
			select.type = RoomType.diningroom;
			select.suitId = i;

			select = findFace(i, false);
			select.type = RoomType.livingroom;
			select.suitId = i;

			select = findFace(i, false);
			select.type = RoomType.bedroom;
			select.suitId = i;

			// select=findFace(i,false);
			// select.type=RoomType.bedroom;
			// select.suitId=i;
			//
			// select=findFace(i,false);
			// select.type=RoomType.kitchen;
			// select.suitId=i;

			select = findFace(i, false);
			select.type = RoomType.bathroom;
			select.suitId = i;
		}

	}

	private Face findFace(int id, boolean isMax) {
		Face select = null;
		if (isMax) {
			int max = Integer.MIN_VALUE;
			for (Face face : geo.faces) {
				if (face.suitId != -1)
					continue;
				int num = 0;
				if (!face.isNearSuit(id))
					num -= 100;
				if (face.isOutside())
					num -= 50;
				num += face.neibourWithoutName();
				if (num > max) {
					max = num;
					select = face;
				}
			}
		} else {
			int min = Integer.MAX_VALUE;
			for (Face face : geo.faces) {
				if (face.suitId != -1)
					continue;
				int num = 0;
				if (!face.isNearSuit(id))
					num += 100;
				num += face.neibourWithoutName();
				if (num < min) {
					min = num;
					select = face;
				}
			}
		}
		return select;
	}

	void printRoomType() {
		for (Face face : geo.faces) {
			face.printName();
		}
	}

}
