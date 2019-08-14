package lesson0104;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import igeo.*;

public class HalfEdgeGeo {
	ArrayList<Edge> edges=new ArrayList<Edge>();
	ArrayList<Face> faces=new ArrayList<Face>();
	ArrayList<Node> nodes=new ArrayList<Node>();
	
	HashMap<Coordinate ,Node> hashmap=new HashMap<Coordinate ,Node>();
	ArrayList<Node> hashNodes = new ArrayList<Node>();

	public void del(){
		for (Edge edge:edges) edge.del2();
		for (Face face:faces) face.del();
		for (Node node:nodes) node.del();
		nodes.clear();
		faces.clear();
		edges.clear();
	}
	
	public HalfEdgeGeo(Geometry geo){
		int num = geo.getNumGeometries();
		for (int i = 0; i < num; i++) {
			Geometry gn = geo.getGeometryN(i);
			if (gn.getNumPoints() < 3)
				continue;
			Coordinate[] coors = gn.getCoordinates();
			for (int j=0,k=coors.length-1;j<k;j++,k--){
				Coordinate tem=coors[j];
				coors[j]=coors[k];
				coors[k]=tem;
			}
			Node node1 = getNode2(coors[0]);
			Node node2 = getNode2(coors[1]);
			Edge first=new Edge(node1,node2);
			Face face=new Face(first);
			edges.add(first);
			faces.add(face);
			first.face = face;
			node1.addEdge(first);
			Edge edge=first;
			Node node=node2;
			for (int j=1;j<coors.length-1;j++){
				Node nextNode = getNode2(coors[j + 1]);
				Edge nextEdge=new Edge(node,nextNode);
				edges.add(nextEdge);
				nextEdge.pre=edge;
				edge.next=nextEdge;
				nextEdge.face = face;
				node.addEdge(nextEdge);
				edge=nextEdge;
				node=nextNode;
			}
			edge.next=first;
			first.pre=edge;
		}
		for (int i=0;i<edges.size();i++){
			Edge edge1=edges.get(i);
			if (edge1.pair!=null) continue;
			for (int j=i+1;j<edges.size();j++){
				Edge edge2=edges.get(j);
				if (edge1.p1==edge2.p2 && edge1.p2==edge2.p1){
					edge1.pair=edge2;
					edge2.pair=edge1;
				}
				
			}
		}
//		nodes.clear();
//		for(Node node:hashmap.values()){
//			nodes.add(node);
//		}
		nodes=hashNodes;
		for (int i=0;i<nodes.size();i++) nodes.get(i).num=i;
		for (int i=0;i<edges.size();i++) edges.get(i).num=i;
		for (int i=0;i<faces.size();i++) faces.get(i).num=i;
	}
	
	public IVec[] outsidePts(){
		Edge first=null;
		for (Edge edge:edges){
			if (edge.pair==null){
				if (!edge.face.isVoid){
					first=edge;
					break;
				}
			}else{
				if (!edge.face.isVoid && edge.pair.face.isVoid){
					first=edge;
				}
			}
		}
		ArrayList<IVec> pts=new ArrayList<IVec>();
		Edge edge=first;
		do{
//			System.out.println(edge.p1.num);
			pts.add(edge.p1.pos().cp());
			for(Edge e:edge.p2.edges){
				if (e.pair==null){
//					System.out.println("null  "+e.face.isVoid);
					if (!e.face.isVoid){
					edge=e;
//					System.out.println("haha");
					break;
					}
				}else{
//					System.out.println("exit "+e.face.isVoid+" "+e.pair.face.isVoid);
					if (!e.face.isVoid && e.pair.face.isVoid){
//						System.out.println("haha");
						edge=e;
						break;
					}
				}
				
			}
		}while(edge!=first);
		return pts.toArray(new IVec[0]);
	}
	
	public IVec[] roomPts(int suitId,RoomType type){
		for (Face face:faces){
			if (face.suitId==suitId && face.type.equals(type)){
				ArrayList<IVec> res=new ArrayList<IVec>();
				Edge edge=face.first;
				do{
					res.add(edge.p1.pos().cp());
					edge=edge.next;
				}while (edge!=face.first);
				return res.toArray(new IVec[0]);
			}
		}
		return null;
	}
	
	public void checkConnection(){
		for (Face face:faces){
			if (face.entrance()==null){
				face.clr(1., 0, 0);
			}
			face.printArea();
		}
	}
	
	public void clr(double r, double g, double b) {
		for (Edge edge : edges) {
			edge.curve.clr(r, g, b);
		}
	}

	public void layer(String st) {
		for (Edge edge : edges) {
			edge.curve.layer(st);
		}
	}

	public void hide() {
		for (Edge edge : edges) {
			edge.curve.hide();
		}
	}

	public void show() {
		for (Edge edge : edges) {
			if (edge.curve==null) edge.curve=new ICurve(edge.p1.pos(),edge.p2.pos());
			edge.curve.show();
		}
	}
	
	private Node getNode(Coordinate coor) {
		if (hashmap.containsKey(coor)) return hashmap.get(coor);
		Node node=new Node(new IVec(coor.x,coor.y));
		hashmap.put(coor, node);
		return node;
	}

	private Node getNode2(Coordinate coor) {
		Node node = new Node(new IVec(coor.x, coor.y));
		for (Node n : hashNodes) {
			if (isSame(node, n))
				return n;
		}
		hashNodes.add(node);
		return node;
	}

	private boolean isSame(Node p1, Node p2) {
		double dist = p1.pos().dif(p2.pos()).len();
		return (dist < Option.halfEdgeGeo_Combine_Tolerance);
	}

	private void deleteEdge(Edge edge) {
		edge.del();
		edges.remove(edge);
		edges.remove(edge.pair);
		nodes.remove(edge.p2);
	}

	private void updateCurve() {
		for (Edge edge : edges) {
			edge.curve.del();
			edge.curve = new ICurve(edge.p1.pos(), edge.p2.pos());
			if (edge.isRoad) {
				edge.curve.clr(1., 0, 0);
			}
		}
	}

	public void combineNode(double dist) {
		for (int i = 0; i < edges.size();) {
			Edge edge = edges.get(i);
			if (edge.dist2() > dist || edge.pair == null) {
				++i;
			} else {
				deleteEdge(edge);
			}
		}
		updateCurve();
	}

	private void SPFA(Node source) {
		for (Node node : nodes) {
			node.dist = Double.MAX_VALUE;
			node.next = null;
		}

		Queue<Node> que = new LinkedList<Node>();
		source.dist = 0;
		que.offer(source);
		while (!que.isEmpty()) {
			Node node = que.poll();
			for (Edge edge : node.edges) {
				if (edge.pair == null)
					continue;
				double dist = edge.dist() + node.dist;
				if (dist < edge.p2.dist) {
					edge.p2.dist = dist;
					que.add(edge.p2);
					edge.p2.next = node;
				}
			}
		}

	}

	private Node SPFA4Road(Node source) {
		if (source.isRoad)
			return source;
		this.SPFA(source);
		Node target = null;
		double min = Double.MAX_VALUE;
		for (Node node : nodes) {
			if (!node.isRoad)
				continue;
			if (node.dist < min) {
				min = node.dist;
				target = node;
			}
		}
		Node node = target;
		while (node.next != null) {
			node.edge(node.next).toRoad();
			node = node.next;
		}
		return target;
	}

	private void targetToRoad(Node target) {
		Node node = target;
		while (node.next != null) {
			node.edge(node.next).toRoad();
			node = node.next; 
		}
	}

	public void generateRoad() {
		Long timer=System.currentTimeMillis();
		ArrayList<Node> nodes = this.coverPoint();
		Node node1 = nodes.get((int) (HRand.random() * nodes.size()));
		Node node2 = nodes.get((int) (HRand.random() * nodes.size()));
		SPFA(node1);
		this.targetToRoad(node2);

		for (int i = 0; i < nodes.size(); i++) {
			if (nodes.get(i).isRoad)
				continue;
			this.SPFA4Road(nodes.get(i));
		}
		timer=System.currentTimeMillis()-timer;
		addMoreRoad(Option.site_Road_Add_Num);
		System.out.println("Generate road done. Cost "+timer/1000d+"s");
	}
	
	private void addMoreRoad(int n){
		int num=nodes.size();
		double dist[][]=new double[num][num];
		double dist2[][]=new double[num][num];
		for (int i=0;i<num;i++){
			for (int j=0;j<num;j++){
				dist[i][j]=Double.MAX_VALUE/3;
				dist2[i][j]=Double.MAX_VALUE/3;
			}
			dist[i][i]=0;
			dist2[i][i]=0;
		}
		
		for (Edge edge:edges){
			if (edge.pair==null) continue;
			dist[edge.p1.num][edge.p2.num]=edge.dist();
		}
		
		for (int k=0;k<num;k++)
			for (int i=0;i<num;i++)
				for (int j=0;j<num;j++){
					if (dist[i][k]+dist[k][j]<dist[i][j]) dist[i][j]=dist[i][k]+dist[k][j];
				}
		
		for (int ii=1;ii<n;ii++){
			
		for (int i=0;i<num;i++){
			for (int j=0;j<num;j++){
				dist2[i][j]=Double.MAX_VALUE/3;
			}
			dist2[i][i]=0;
		}
		
		for (Edge edge:edges){
			if (edge.pair==null) continue;
			if (edge.isRoad) dist2[edge.p1.num][edge.p2.num]=edge.dist();
		}
		
		for (int k=0;k<num;k++)
			for (int i=0;i<num;i++)
				for (int j=0;j<num;j++){
					if (dist2[i][k]+dist2[k][j]<dist2[i][j]) dist2[i][j]=dist2[i][k]+dist2[k][j];
				}
		Node select1=null,select2=null;
		double max=-1;
		for (int i=0;i<num;i++)
			for (int j=0;j<num;j++){
				Node node1=nodes.get(i);
				Node node2=nodes.get(j);
				if (node1.isRoad && node2.isRoad){
					double dist3=dist2[node1.num][node2.num]-dist[node1.num][node2.num];
					if (dist3>max){
						select1=node1;
						select2=node2;
						max=dist3;
					}
				}
			}
		SPFA(select1);
		this.targetToRoad(select2);
		}
		
	}
	private int checkBuildingIsConnected(){
		int num=0;
		for (Face face:faces){
			Edge edge=face.first;
			face.connect=false;
			do{
				if (edge.p1.isRoad) {
					face.connect=true;
					break;
				}
				edge=edge.next;
			}while(edge!=face.first);
			if (!face.connect) ++num;
			
		}
		return num;
	}

	public ArrayList<Node> coverPoint() {
		ArrayList<Node> res = new ArrayList<Node>();
		for (int i=0;i<Option.site_Entrance.length;++i){
			Node select=null;
			double min=Double.MAX_VALUE;
			for (Node node:nodes){
				if (node.isOutside()){
					double dist=node.pos().dist(Option.site_Entrance[i]);
					if (dist<min){
						select=node;
						min=dist;
					}
				}
			}
			select.isRoad=true;
			new IPoint(select.pos()).layer(Option.layer_Road_Point);
			res.add(select);
		}
		int count = checkBuildingIsConnected();
		for (Node node:res) node.isRoad=false;
		while (count > 0) {
			Node select = null;
			int max = -1;
			for (Node node : nodes) {
				if (node.isOutside()) continue;
				int num = 0;
				for (Edge edge : node.edges) {
					if (!edge.face.connect)
						++num;
				}
				if (num > max) {
					max = num;
					select = node;
				}
			}
			res.add(select);
			new IPoint(select.pos()).layer(Option.layer_Road_Point);
			for (Edge edge : select.edges) {
				edge.face.connect = true;
			}

			count -= max;

		}
		return res;
	}
	
	public void steinerTree(ArrayList<Node> target){
		int num=nodes.size();
		int tNum=target.size();
		int endSt=1<<tNum;
		int[][] dptree=new int[num][endSt];
		int[] st=new int[num];
		boolean[][] vis=new boolean[num][endSt];
		Queue<Integer> que=new LinkedList<Integer>();
	
		//iniSt
		for (int i=0;i<num;i++)
			for (int j=0;j<endSt;j++) dptree[i][j]=-1;
		for (int i=0;i<num;i++) {
			st[i]=0;
			if (target.contains(nodes.get(i))) st[i]=1<<target.indexOf(nodes.get(i));
		}
		
		// stTree
		for(int j=1;j<endSt;j++){  
	        for(int i=0;i<num;i++){  
	            if(st[i]!=0 && (st[i]&j)==0) continue;  
	            for(int sub=(j-1)&j;sub!=0;sub=(sub-1)&j){  
	                int x=st[i]|sub,y=st[i]|(j-sub);  
	                if(dptree[i][x]!=-1 && dptree[i][y]!=-1)  
	                    update(dptree[i][j],dptree[i][x]+dptree[i][y]);  
	            }  
	            if(dptree[i][j]!=-1)   {
	                que.add(new Integer(i));
	                vis[i][j]=true;  
	            }
	        }  
	        
	        //SPFA
	        int state=j; 
	        while(!que.isEmpty()){  
	            int u=que.poll();   
	            vis[u][state]=false;
	            Node node=nodes.get(u);
	            for (Edge edge:node.edges){  
	                int v=nodes.indexOf(edge.p2);  
	                if(dptree[v][st[v]|state]==-1 ||   
	                    dptree[v][st[v]|state]>dptree[u][state]+edge.dist()){  
	      
	                    dptree[v][st[v]|state]=dptree[u][state]+edge.dist();  
	                    if((st[v]|state)!=state || vis[v][state])   
	                        continue; //只更新当前连通状态  
	                    vis[v][state]=true;  
	                    que.add(v);  
	                }  
	            }  
	        }  
	    } 
		int min=1000000000;
		for (int i=0;i<num;i++){
			if (dptree[i][endSt-1]<min && dptree[i][endSt-1]>0) min=dptree[i][endSt-1];
			System.out.println(dptree[i][endSt-1]);
		}
		System.out.println(min);

	}
	
	private int update(int a,int x){
		return (a>x || a==-1)? x : a;  
	}

}

class Edge{    public int num;
	Edge pre,next,pair;
	Node p1,p2;
	Face face;
	ICurve curve;
	boolean isRoad = false;
	int dist;
	
	public Edge(Node p1,Node p2){
		this.p1=p1;
		this.p2=p2;
	}
	
	public void del2(){
		pre=null;
		next=null;
		pair=null;
		p1=null;
		p2=null;
		face=null;
		if (curve!=null){
			curve.hide();
			curve.layer(Option.layer_Useless);
		}
		curve=null;
	}

	public void toRoad() {
		curve.clr(1., 0, 0);
		isRoad = true;
		p1.isRoad = true;
		p2.isRoad = true;
		if (pair != null) {
			pair.isRoad = true;
			pair.curve.clr(1., 0, 0);
		}
	}
	
	public int dist(){
		if (dist==0) dist=(int)p1.pos.dist(p2.pos);
		return dist;
	}

	public double dist2() {
		return p1.pos.dist(p2.pos);
	}

	public void del() {
		if (face.first == this)
			face.first = next;
		if (pair.face.first == pair)
			pair.face.first = pair.next;
		curve.del();
		pair.curve.del();
		pre.next = next;
		next.pre = pre;
		pair.pre.next = pair.next;
		pair.next.pre = pair.pre;
		p1.edges.remove(this);
		p2.edges.remove(pair);
		p1.edges.addAll(p2.edges);
		next.p1 = p1;
		pair.pre.p2 = p1;
		for (Edge edge : p2.edges) {
			edge.p1 = p1;
			if (edge.pair != null)
				edge.pair.p2 = p1;
		}
	}
}

class Face{
	public int num;
	Edge first;
	double area,targetArea;
	boolean connect = false;
	RoomType type;
	int suitId=-1;
	IText text;
	boolean isBuilding;
	boolean isVoid=false;
	
//	ICurve envolope;
	
	
	public Face(Edge first){
		this.first=first;
	}
	
	public void del(){
		first=null;
		type=null;
		text=null;
	}
	
	public int neibourWithoutName(){
		Edge edge=first;
		int count=0;
		do{
		if (edge.pair!=null)
			if (edge.pair.face.suitId==-1) 
				++count;
		edge=edge.next;
		} while(edge!=first);
		return count;
	}
	
	public boolean crossCurve(ICurve curve){
		if (curve == null)
			return false;
		Edge edge=first;
		int count=0;
		do{
		if (curve.isInside2d(edge.p1.pos())) return true;
		edge=edge.next;
		} while(edge!=first);
		return false;
	}
	
	public boolean isNearSuit(int id){
		Edge edge=first;
		do{
		if (edge.pair!=null)
			if (edge.pair.face.suitId==id) 
				return true;
		edge=edge.next;
		} while(edge!=first);
		return false;
	}
	
	public void printName(){
		DecimalFormat df = new DecimalFormat("#.00");
		if (suitId==-1) return;
		if (text==null){
			text = new IText(suitId + "\n" + type + "\n" + df.format(area), 500, center());
		} else{
			text.del();
			text = new IText(suitId + "\n" + type + "\n" + df.format(area)+" of "+df.format(targetArea), 500, center());
		}
	}
	
	public void printArea(){
		DecimalFormat df = new DecimalFormat("#.00");
		if (text==null){
			text = new IText(df.format(IGeo_Math.getArea(this.pts())), 500, center().dif(1000, 500, 0));
		} else{
			text.del();
			text = new IText(df.format(IGeo_Math.getArea(this.pts())), 500, center().dif(1000, 500, 0));
		}
	}
	
	public void clr(double r,double g,double b){
		Edge edge=first;
		do{
		edge.curve.clr(r*=0.9,g,b);
		edge.curve.weight(10);
		edge=edge.next;
		} while(edge!=first);
	}
	
	public void test(){
		IG.extrude(pts(), 10000);
	}
	
	public IVec[]  envolope(){
		IVec[] res=new IVec[4];
		double min=Double.MAX_VALUE;
		IVec[] pts=pts();
		Edge edge=first;
		do {
			IVec u=edge.p2.pos.dif(edge.p1.pos).unit();
			IVec v=u.cp().rot(Math.PI/2);
			double minX=Double.MAX_VALUE;
			double maxX=Double.MIN_VALUE;
			double minY=Double.MAX_VALUE;
			double maxY=Double.MIN_VALUE;
			for (IVec vec:pts){
				double x=vec.dot(u);
				double y=vec.dot(v);
				if (x<minX) minX=x;
				if (x>maxX) maxX=x;
				if (y<minY) minY=y;
				if (y>maxY) maxY=y;
			}
			double area=(maxX-minX)*(maxY-minY);
			if (area<min){
				min =area;
				res=new IVec[4];
				res[0]=u.cp().mul(minX).sum(v.cp().mul(minY));
				res[1]=u.cp().mul(maxX).sum(v.cp().mul(minY));
				res[2]=u.cp().mul(maxX).sum(v.cp().mul(maxY));
				res[3]=u.cp().mul(minX).sum(v.cp().mul(maxY));
				
			}
			edge=edge.next;
		}while(edge!=first);
//		if (envolope!=null) envolope.del();
//		envolope=new ICurve(res,1,true);
		return res;
	}
	
	public IVec[] pts(){
		ArrayList<IVec> pts=new ArrayList<IVec>();
		Edge edge=first;
		do{
			pts.add(edge.p1.pos());
			edge=edge.next;
		} while (edge!=first);
		IVec[] res=new IVec[pts.size()];
		for (int i=0;i<pts.size();i++){
			res[i]=pts.get(i);
		}
		return res;
	}
	
	public IVec center(){
		Edge edge=first;
		int count=0;
		IVec res=new IVec();
		do{
			count++;
			res.add(edge.p1.pos());
			edge=edge.next;
		}while(edge!=first);
		res.mul(1./count);
		return res;
	}
	
	public IVec entrance(){
		Edge edge=first;
		do{ 
			if (edge.p1.isRoad) return edge.p1.pos();
			edge=edge.next;
		}while(edge!=first);
		return edge.p1.pos();
	}
	
	public boolean isNear(Face face){
		Edge edge=first;
		do{
			if (edge.pair!=null)
				if (edge.pair.face==face)
					return true;
			edge=edge.next;
		}while(edge!=first);
		
		return false;
	}
	
	public boolean isOutside(){
		Edge edge=first;
		do{
			if (edge.pair==null) return true;
			edge=edge.next;
		}while(edge!=first);
		
		return false;
	}
}

class Node{
	IVec pos;
	ArrayList<Edge> edges = new ArrayList<Edge>();
	boolean isRoad = false;
	Node next;
	double dist;
	int num;
	
	public void del(){
		pos=null;
		edges.clear();
		next=null;
		
	}

	public Node(IVec pos){
		this.pos=pos;
	}
	
	public IVec pos(){
		return pos;
	}

	public void addEdge(Edge edge) {
		edges.add(edge);
	}

	public Edge edge(Node p2) {
		for (Edge edge : edges) {
			if (edge.p2 == p2)
				return edge;
		}
		return null;
	}

	public ArrayList<Face> faces() {
		ArrayList<Face> res = new ArrayList<Face>();
		for (Edge edge : edges) {
			res.add(edge.face);
		}
		return res;
	}

	public boolean isOutside() {
		for (Edge edge : edges) {
			if (edge.pair == null)
				return true;
		}
		return false;
	}
}