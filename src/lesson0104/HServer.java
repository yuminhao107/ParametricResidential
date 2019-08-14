package lesson0104;

import processing.core.PApplet;
import java.util.ArrayList;

public class HServer implements Runnable {
	public double updateRate = 30d / 1000; // in seconds
	public boolean running = false;
	public int time = 0, duration = -1;
	Thread thread = null;
	public PApplet main;
	public ArrayList<HAgent> agents = new ArrayList<HAgent>();
	BuildingPlan plan;

	public HServer(PApplet app) {
		main = app;
	}

	public HServer() {
	}

	public HServer(BuildingPlan plan) {
		this.plan = plan;
	}

	public void addAgent(HAgent agent) {
		agents.add(agent);
	}

	public void start() {
		thread = new Thread(this);
		running = true;
		time = 0;
		thread.start();
	}

	public void pause() {
		running = false;
	}

	public void resume() {
		running = true;
	}

	public void stop() {
		running = false;
		thread = null;
	}

	public int time() {
		return time;
	}

	public void duration(int dur) {
		duration = dur;
	}

	public void step() {
		for (int i = 0; i < agents.size(); i++)
			agents.get(i).interact(agents);
		for (int i = 0; i < agents.size(); i++)
			agents.get(i).update();
	}

	public void drawAgents() {
		for (int i = 0; i < agents.size(); i++)
			agents.get(i).drawGraphic();
	}

	public void checkBuil() {
		if (plan.isStill()) {
			plan.generateHalfEdgeGeo();
			if (plan.connectionIsOK()) {
				plan.geo.show();
				plan.checkColor();
				CalUnit4Buil cal = new CalUnit4Buil(plan);
				cal.start();
				plan.sitePlan.generateNextBuilding();
			} else {
				plan.geo.del();
				plan.tryNum++;
//				plan.server.stop();
//				for (HTL htl : plan.htls)
//					htl.del();
//				for (HCircle p : plan.points)
//					p.del();
//				plan.htls.clear();
//				plan.points.clear();
				if (plan.tryNum<Option.building_Try_Num) {
					plan.initialize2();
				}else{
					plan.face.isBuilding=false;
				}
				
			}  
		}
	}

	public void run() {
		Thread thisThread = Thread.currentThread();
		while (thread == thisThread) {
			if (duration >= 0 && time >= duration) {
				stop();
				break;
			}
			if (running) {
				step();
				checkBuil();
			}
			try {
				Thread.sleep((int) (updateRate * 1000));
			} catch (InterruptedException e) {
			}
			time++;
		}
	}
}
