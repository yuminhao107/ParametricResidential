package lesson0104;

import igeo.IVec;

public class Option {
	// ȫ���������
	public static long random_Seed = 303030;// 123,12345678 303030
	// �����������
	public static long random_Seed_Building=123;
	
	// ���ɽ���������
	public static int thread_Num = 64;
	
	//�Ƿ����
	public static boolean print_Building_Card=false;

	// ���صĲ���
	// ��·���
	public static double streetWidth = 8000;
	//С·���
	public static double walk_Width=2000;
	//�Ƿ����С·
	public static boolean draw_Walk=true;
	// �ؿ�����ο�ֵ ��λ ƽ��
	public static double site_Single_Area = 700;
	//������ӵ�·ʹ��·ϵͳ�ɻ� ��������
	public static int site_Road_Add_Num=4;

	// ���ؼ��Ŵ�����
	//��ֹ���� ����n�γ���ʧ�ܽ������Ŵ�
	public static int site_End_Num = 800;
	// ������С����Ҫ��
	public static double site_Angle_Importace = 2;
	// ����Ƚӽ�1
	public static double site_Shape_Importace = 3;
	// ����
	public static double site_Direction_Importace = 1;
	// ���
	public static double site_Area_Importace = 2;
	// �ӽ�����
	public static double site_Rect_Importace = 1;
	// ��·ƽ��
	public static double site_Road_Importace = 1;

	// ��������
	// ���õ�·���ߵľ���
	public static double building_Offset = 5000;
	// ���ɽ�������Դ���
	public static int building_Try_Num = 4;
	// ��������ο�ֵ ��λ ƽ��
	public static double building_Single_Suit_Area = 70;
	// ���������
	public static int building_Suit_Max_Num = 4;
	// ��С������
	public static int building_Suit_Min_Num = 2;
	// �Ƿ�ı�������
	public static boolean building_Change_Outside_Point = true;
	// �������
	public static double building_Height = 3000;
	//Ů��ǽǽ��
	public static double building_Height_NvErQiang = 500;
	//������
	public static double building_Window_Width = 800;
	//�����߶�
	public static double building_Window_Height = 2000;

	// ������������̶�
	public static double building_Shape_Tol = 0.7;

	// ����������̶�
	public static double building_area_Tol = 0.6;

	// �������Ŵ�����
	// ��ֹ����
	public static int building_End_Num = 400;
	// ������ǿ��
	public static double building_Tension_Hardness = 5;
	// Ħ��ϵ��
	public static double building_Fric = 0.02;
	// ������
	public static int building_Cal_Duration = 1;
	// ������С����Ҫ��
	public static double building_Angle_Importace = 1;
	// ����Ƚӽ�1
	public static double building_Shape_Importace = 2.5;
	// ���
	public static double building_Area_Importace = 1;
	// �ӽ�����
	public static double building_Rect_Importace = 1.6;
	// ���Ӵ�����С���
	public static double building_Door_Min_Dist = 1600;
	// ����Ҫ��
	public static double building_Door_Importace = 3;

	// ��߽ṹ�Ĳ���
	// �ϲ��������� ��λ����
	public static double halfEdgeGeo_Combine_Tolerance = 0.000001;

	//����3dmͼ����������
	// ����ͼ�������
	public static String layer_Useless = "useless";
	//��·
	public static String layer_Road="Road";
	// ������ǽ
	public static String layer_Wall_Outside = "Building_Wall_Outside";
	// ������ǽ
	public static String layer_Wall_Outside_White = "Building_Wall_Outside_White";
	// ����¥��
	public static String layer_Wall_Floor = "Building_Floor";
	//��������
	public static String layer_Wall_Window = "Building_Window";
	// ��·�ڵ�
	public static String layer_Road_Point = "roadPoint";
	// ������ڿռ�
	public static String layer_Area_Entrance = "curve_Entrance";
	// ����¥�ݿռ�
	public static String layer_Area_Stair = "curve_Stair";
    // ����
	public static String layer_Building_Card="Card";
	// ����������ÿ����ȥ�ķ���������
	public static int[][] voidNum = { { 0, 0, 0, 0 }, { 2, 1, 0, 0 }, { 4, 1, 2, 2 }, { 4, 2, 2, 3 }, { 4, 3, 3, 4 } };
   
	
	//���س����λ�� ��3dm�ļ�����
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
