package lesson0104;

import java.util.ArrayList;

import igeo.*;

public class IPoly extends HAgent {
	IVec[] pts;

	public IPoly(HServer server,IVec[] pts) {
		this.server=server;
		server.addAgent(this);
		this.pts = pts;
	}

	public void interact(ArrayList<HAgent> agents) {
		for (int i = 0; i < agents.size(); i++) {
			if (agents.get(i) instanceof HCircle) {
				HCircle c = (HCircle) agents.get(i);
				IVec pos = c.pos;
				double min = Double.MAX_VALUE;
				int flag = 0;
				for (int j = 0; j < pts.length; ++j) {
					double l = dist(pts[j], pts[next(j)], pos);
					if (l < min) {
						min = l;
						flag = j;
					}
				}
				double d = c.radius - min;
				if (d > 0) {
					IVec f = pts[flag].dif(pts[next(flag)]);
					f = new IVec(-f.y, f.x);
					f.len(-10000 - d * 10);
					c.push(f);
				}
			}
		}
	}

	int next(int a) {
		if (a == pts.length - 1)
			return 0;
		return a + 1;
	}

	double dist(IVec p1, IVec p2, IVec p) {
		IVec p12 = p2.dif(p1);
		p12.len(1d);
		p12 = new IVec(-p12.y, p12.x);
		IVec p10 = p1.dif(p);
		return -p10.dot(p12);
	}
}

class HCircle extends HParticle {
	double radius;
	RoomType type;
	int suitId;
	ICircle circle;
	double targetArea;

	public HCircle(HServer server,IVec pos, RoomType type, int suitId) {
		super(server,pos);
		this.type = type;
		this.suitId = suitId;
		radius = Math.sqrt(type.area) / 3.14;
		this.fric(Option.building_Fric);
	}

	public void interact(ArrayList<HAgent> agents) {
		for (int i = 0; i < agents.size(); i++) {
			if (agents.get(i) instanceof HCircle) {
				HCircle c = (HCircle) agents.get(i);
				if (c == this)
					continue;
				double dist = (radius + c.radius)*1.5 - pos().dif(c.pos()).len();
				if (dist > 0) {
					IVec f = c.pos().dif(pos()).len(1000 + dist);
					c.push(f);
				}
			}
		}
	}

	public void update() {
		if (circle == null) {
			circle = new ICircle(pos(), radius);
		} else {
			circle.center(pos()).radius(radius);
		}
		
		if (fixed) return;
		v.add(a.mul(server.updateRate));
		a = new IVec();
		pos.add(v.cp().mul(server.updateRate));
		v.mul(1-fric);
	}

	public void del() {
		circle.hide().layer(Option.layer_Useless);
		super.del();
	}

}

class HTL extends HAgent {
	HCircle pt1, pt2;
	double len, tension;
	ICurve line;

	public HTL(HServer server,HCircle p1, HCircle p2) {
		this.server = server;
		server.addAgent(this);
		this.pt1 = p1;
		this.pt2 = p2;
		len = p1.radius + p2.radius;
//		len *= 0.8;
		tension = Option.building_Tension_Hardness;
	}

	public void interact(ArrayList<HAgent> agents) {
		IVec dif = pt2.pos().dif(pt1.pos());
		double dl = dif.len();
		dif.len(dl - len).mul(tension);
		pt1.push(dif);
		pt2.pull(dif); // opposite force
	}

	public void update() {
		if (line == null) {
			line = new ICurve(pt1.pos(), pt2.pos());
			// line.hide();
		} else {
			line.updateGraphic();
		}
	}

	public void del() {
		line.hide();
		line.layer(Option.layer_Useless);
		super.del();
	}

}
