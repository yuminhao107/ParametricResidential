package lesson0104;

import igeo.ICurve;

public class CalUnit extends Thread {
	SitePlan plan;

	public CalUnit(SitePlan plan) {
		this.plan = plan;
	}

	public void run() {
		for (int i = 1; plan.changeNum<Option.site_End_Num; i++) {
			System.out.println("Try change  "+i);
			plan.tryChange();
			// if (i % 10000 == 0)
			// plan.geo.combineNode(plan.randomR);
		}
		System.out.println("done");
		// plan.generateDistrict();

	}
}
