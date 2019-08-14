package lesson0104;

public enum RoomType {

	entrance(10, 1, new RoomType[] {}), stair(10, 0.01, new RoomType[] { entrance }), corridor(10, 0.01,
			new RoomType[] { stair }), livingroom(25, 0.02, new RoomType[] { entrance, stair, corridor }), bedroom(15,
					0.2, new RoomType[] { livingroom }), bathroom(7, 0.2,
							new RoomType[] { livingroom, bedroom }), diningroom(10, 0.2,
									new RoomType[] { livingroom }), kitchen(7, 0.2,
											new RoomType[] { diningroom }), courtyard(0, 0.5, new RoomType[] {});

	RoomType[] rooms;
	double area;
	double connectionImportance;// 0~1,0 is most important

	RoomType(double area, double importance, RoomType[] rooms) {
		this.area = area * 1000000;
		this.connectionImportance = importance;
		this.rooms = rooms;
	}
}
