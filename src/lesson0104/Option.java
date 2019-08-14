package lesson0104;

import igeo.IVec;

public class Option {
	// 全局随机种子
	public static long random_Seed = 303030;// 123,12345678 303030
	// 建筑随机种子
	public static long random_Seed_Building=123;
	
	// 生成建筑进程数
	public static int thread_Num = 64;
	
	//是否出板
	public static boolean print_Building_Card=false;

	// 场地的参数
	// 道路宽度
	public static double streetWidth = 8000;
	//小路宽度
	public static double walk_Width=2000;
	//是否绘制小路
	public static boolean draw_Walk=true;
	// 地块面积参考值 单位 平米
	public static double site_Single_Area = 700;
	//额外添加道路使道路系统成环 环的数量
	public static int site_Road_Add_Num=4;

	// 场地简单遗传参数
	//终止条件 连续n次尝试失败结束简单遗传
	public static int site_End_Num = 800;
	// 消除最小脚重要性
	public static double site_Angle_Importace = 2;
	// 长宽比接近1
	public static double site_Shape_Importace = 3;
	// 方向
	public static double site_Direction_Importace = 1;
	// 面积
	public static double site_Area_Importace = 2;
	// 接近矩形
	public static double site_Rect_Importace = 1;
	// 道路平滑
	public static double site_Road_Importace = 1;

	// 建筑参数
	// 退让道路中线的距离
	public static double building_Offset = 5000;
	// 生成建筑最大尝试次数
	public static int building_Try_Num = 4;
	// 套型面积参考值 单位 平米
	public static double building_Single_Suit_Area = 70;
	// 最大套型数
	public static int building_Suit_Max_Num = 4;
	// 最小套型数
	public static int building_Suit_Min_Num = 2;
	// 是否改变外轮廓
	public static boolean building_Change_Outside_Point = true;
	// 建筑层高
	public static double building_Height = 3000;
	//女儿墙墙高
	public static double building_Height_NvErQiang = 500;
	//窗框宽度
	public static double building_Window_Width = 800;
	//窗户高度
	public static double building_Window_Height = 2000;

	// 建筑长宽比容忍度
	public static double building_Shape_Tol = 0.7;

	// 建筑面积容忍度
	public static double building_area_Tol = 0.6;

	// 建筑简单遗传参数
	// 终止条件
	public static int building_End_Num = 400;
	// 弹力线强度
	public static double building_Tension_Hardness = 5;
	// 摩擦系数
	public static double building_Fric = 0.02;
	// 计算间隔
	public static int building_Cal_Duration = 1;
	// 消除最小脚重要性
	public static double building_Angle_Importace = 1;
	// 长宽比接近1
	public static double building_Shape_Importace = 2.5;
	// 面积
	public static double building_Area_Importace = 1;
	// 接近矩形
	public static double building_Rect_Importace = 1.6;
	// 连接处的最小宽度
	public static double building_Door_Min_Dist = 1600;
	// 其重要性
	public static double building_Door_Importace = 3;

	// 半边结构的参数
	// 合并顶点的误差 单位毫米
	public static double halfEdgeGeo_Combine_Tolerance = 0.000001;

	//导出3dm图层名字设置
	// 无用图层的名字
	public static String layer_Useless = "useless";
	//道路
	public static String layer_Road="Road";
	// 建筑外墙
	public static String layer_Wall_Outside = "Building_Wall_Outside";
	// 建筑外墙
	public static String layer_Wall_Outside_White = "Building_Wall_Outside_White";
	// 建筑楼板
	public static String layer_Wall_Floor = "Building_Floor";
	//建筑窗户
	public static String layer_Wall_Window = "Building_Window";
	// 道路节点
	public static String layer_Road_Point = "roadPoint";
	// 建筑入口空间
	public static String layer_Area_Entrance = "curve_Entrance";
	// 建筑楼梯空间
	public static String layer_Area_Stair = "curve_Stair";
    // 出板
	public static String layer_Building_Card="Card";
	// 建筑层数及每层挖去的房间数控制
	public static int[][] voidNum = { { 0, 0, 0, 0 }, { 2, 1, 0, 0 }, { 4, 1, 2, 2 }, { 4, 2, 2, 3 }, { 4, 3, 3, 4 } };
   
	
	//场地出入口位置 由3dm文件设置
	public static IVec[] site_Entrance;

}

class Print{
	private static int lineNum=0;
	private static int rowNum=0;
	private static double lineDis=35000;
	private static double rowDis=35000;
	private static int maxRow=8;
	private static IVec fixOrigin=new IVec(1000000,250000);
    public static int txtSize=5000;

	public static IVec printOrigin(){
	  IVec v= new IVec(rowNum*rowDis,lineNum*lineDis,0);
	  v.add(fixOrigin); 
	  rowNum=rowNum+1;
	  if (rowNum==maxRow) {
	   rowNum=0;
	   lineNum++; 
	  }
	  return v;
	  
	}

	public static void printOriginFix(){
	   fixOrigin.add(new IVec(rowNum*rowDis,lineNum*lineDis,0));
	   lineNum=0;
	   rowNum=0;
	  
	}

	public static void printOriginFix(IVec v){
	   fixOrigin=v;
	   lineNum=0;
	   rowNum=0;
	  
	}
}
