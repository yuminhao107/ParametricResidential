package lesson0104;

public class CalUnit4Buil extends Thread{
	BuildingPlan plan;
	
	public CalUnit4Buil(BuildingPlan plan){
		this.plan=plan;
	}
	
	public void run() {
		while(plan.changeNum<Option.building_End_Num) {
			plan.change1();
			
			// plan.change2();
			// if (i % 10000 == 0)
			// plan.geo.combineNode(plan.randomR);
			
			try {
				Thread.sleep(Option.building_Cal_Duration);
			} catch (InterruptedException e) {
			}
		}
		plan.drawUpstair();
		// System.out.println("done");
		// plan.generateDistrict();

	}

}
