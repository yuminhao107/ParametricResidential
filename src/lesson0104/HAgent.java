package lesson0104;

import java.util.ArrayList;

public class HAgent {
	public HServer server;
	public void interact(ArrayList<HAgent> agents) {}
	public void update() {}
	public void drawGraphic() {}
	public void del(){
		server.agents.remove(this);
	}
}
