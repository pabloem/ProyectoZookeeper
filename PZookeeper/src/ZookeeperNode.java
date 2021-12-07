
public class ZookeeperNode {
 
	public static void main(String[] args) {
		ZookeeperNode node = new ZookeeperNode();
		node.create(“myapplication/directory/path”, null);
		if(!node.exists(“myapplication/directory/path”, false)) {
			System.out.println("ERROR! - myapplication/directory/path deberia existir pero no existe");
		}
		if(node.exists(“myapplication/directory/otherpath”, false)) {
			System.out.println("ERROR! - myapplication/directory/otherpath NO deberia existir pero si existe");
		}	

	}
	
	public boolean create(String path, bytes[] data) {
		if (this.exists(path)) return false;
	}
	
	public boolean exists(String path, Boolean watch) {
		// Implementemos este metodo primero
	}
}
