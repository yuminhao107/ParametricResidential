package lesson0104;

import java.util.ArrayList;

import igeo.*;

public class HParticle extends HPoint implements IVecI {
	double fric = 0d;
	double m = 1d;
	public IVec v = new IVec();
	public IVec a = new IVec();
	boolean fixed=false;
	
	public HParticle(HServer server, IVecI pos) {
		super(server,pos);
	}
	
	public HParticle fric(double f) {
		fric = f;
		return this;
	}
	
	public HParticle push(IVecI f) {
		a.add(f.cp().mul(1/m));
		return this;
	}
	
	public HParticle pull(IVecI f) {
		a.add(f.cp().flip().mul(1/m));
		return this;
	}
	
	public void update() {
		if (fixed) return;
		v.add(a.mul(server.updateRate));
		a = new IVec();
		pos.add(v.cp().mul(server.updateRate));
		v.mul(1-fric);
	}
	
	public IVec vel(){
		return v;
	}
	
	public void fix(){
		fixed=true;
	}

}
