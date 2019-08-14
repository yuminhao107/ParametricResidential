package lesson0104;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Geometry;

import igeo.*;

import processing.core.PApplet;

public class A_main extends PApplet {
	/*
	 * 运行时需要顺序按下 U I O 三键 按下 U 开始场地简单遗传运算 场地遗传运算完后，按下 I 显示路网 显示路网后，按下 O 开始生成建筑
	 */
	ICurve surrounding;      //场地边界
	ArrayList<ICurve> sites;
	SitePlan plan;           //场地平面
	CalUnit cal;             //多线程计算
	double unit;             //尺度参考
	
	boolean flag;

	BuildingPlan buil;

	public void setup() {
		size(1280, 720, IG.GL);
		IG.open("0106.3dm");
		int pointNum=IG.layer("entrance").ptNum();
		Option.site_Entrance=new IVec[pointNum];
		for (int i=0;i<pointNum;i++){
			Option.site_Entrance[i]=IG.layer("entrance").point(i).pos();
		}
		
        //合并场地边界曲线；
		ICurve line1 = IG.layer("site").curves()[0];
		ICurve line2 = IG.layer("site").curves()[1];
		int num1 = line1.cpNum();
		int num2 = line2.cpNum();
		IVec pts[] = new IVec[num1 + num2 - 2];
		for (int i = 0; i < num1; i++)
			pts[i] = line1.cp(i);
		for (int i = num1; i < num1 + num2 - 2; i++)
			pts[i] = line2.cp(i - num1 + 1);

		surrounding = new ICurve(pts, 1, true);

		unit=surrounding.len()/1000;
		double area = IGeo_Math.getArea(surrounding) / 1000 / 1000;
		System.out.println("Site area is  "+area);
		int siteNum = (int) (area / Option.site_Single_Area); 

		ICurve river1 = IG.layer("river").curves()[0];
		river1.clr(0, 0, 1.);
		ICurve river2 = IG.layer("river").curves()[1];
		river2.clr(0, 0, 1.);
		Geometry geo = IGeo_Math.centerVoronoi(surrounding, siteNum, 50);// 50
		HalfEdgeGeo half = new HalfEdgeGeo(geo);
		plan = new SitePlan(half,unit);
		plan.green=IG.layer("green").curves()[0];
		plan.addFiedld(new ICurveAttractorField(surrounding).intensity(-20).gaussianDecay(100*unit)); 

		plan.addRiver(river1);
		plan.addRiver(river2);
		plan.addFiedld(new ICurveAttractorField(river1).intensity(20).gaussianDecay(100 * unit)); 
		plan.addFiedld(new ICurveAttractorField(river2).intensity(20).gaussianDecay(100 * unit));

		cal = new CalUnit(plan);
		
		IG.perspective();
		IG.top();
		IG.background(1d);
		 
		plan.geo.generateRoad();

	}

	public void draw() {
		for (ICurve curve : IG.curves()) {
			curve.updateGraphic();
		}
	}

	public void keyPressed() {
		if (key == 'u')
			cal.start();
		if (key == 'i') {
			cal.stop();
			plan.geo.hide();
			plan.drawRoad();

		}
		if (key=='o'){
			plan.generateBuilding();

		}
		if (key=='y'){
			plan.drawWalk();
		}

	}
	
	public void openFile(String s){
		String path = "";
		if (System.getProperty("os.name").indexOf("Mac") >= 0) {
			path = "/Users/ming/OneDrive/文档/Java/";
		} else {
			path = "c:/Users/YMH/OneDrive/文档/Java/";
		}
		IG.open(path + s);
	}

	public void saveImage() {
		String path = "/Users/ming/Desktop/images/003/" + frameCount + ".jpg";
		save(path);
	}

}
