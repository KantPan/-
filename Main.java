
import java.io.*;
import java.util.*;
import util.*;


public class Main{
	public static String PATH = "E:\\competition\\TIANCHI\\CAINIAO2\\DATA\\";
	public static String OUTPUTFILE = null;
	public static boolean PRINT = true;

	public static HashMap<String, Point> ADDRS = new HashMap<>();
	public static HashMap<String, ArrayList<Order>> ORDERS = new HashMap<>();
	public static ArrayList<String> KEYS = new ArrayList<>();
	public static ArrayList<Courier> COURIERS = new ArrayList<>();

	public static int ORDER_NUM = 0;
	public static String COST_TIME = "";

	public static void main(String[] args) throws Exception {
		Main M = new Main();
		M.getData();
		M.curOrderNum();

		ArrayList<Order> orders = new ArrayList<Order>();

		for (Courier c : COURIERS)
		{
			if(c.curAddr.Addr!=null)
			{
			orders = ORDERS.get(c.curAddr.Addr);
			if (orders.isEmpty())
				continue;
			orders = c.pickO(orders, PRINT);
			if (!orders.isEmpty())
				M.deleteOrders(c.curAddr.Addr, orders);
			c.deliverO(PRINT);
			}
		}
		
		String key = "";
		
		while (ORDER_NUM > 0)
		{
			
			for (int i = 0; i < KEYS.size(); i++)
			{
				key = KEYS.get(i);
				orders = ORDERS.get(key);
				if (orders.isEmpty())
					continue;

				if (key.substring(0,1).equals("S"))
					orders = M.sortOrders(orders);

				M.sortCouriers(orders, 0.65);

				orders = COURIERS.get(0).pickO(orders, PRINT);
				if (!orders.isEmpty())
					M.deleteOrders(key, orders);
				COURIERS.get(0).deliverO(PRINT);
		
			}
			

			M.curOrderNum();
			M.updateCostTime();	
			System.out.println("未处理订单数：" + ORDER_NUM + "--" + 
				COST_TIME);	
		}
		M.printToFile();		
	}	

//====================================================================

/*
	public void sortCouriers(Courier c){
		double temp = 0.0;
		double alpha = 12;
		Order o = O.get(0);
		for (String key : KEYS)
		{
			if (ORDERS.get(key).size() > 0)
			{
			
			sortOrders(key);
			temp = c.curTime + c.curAddr.distance(o.From) / 250.0;
			if (o.From.Addr.substring(0,1).equals("A"))
			{
				temp = temp > o.Pickup_time ? (temp - c.curTime + alpha * (temp - o.Pickup_time)) : (temp - c.curTime);
			}
			else
			{
				temp = temp > o.Pickup_time ? (temp - c.curTime + alpha * (temp - o.Pickup_time)) : 
					(o.Pickup_time - c.curTime);
			}
			c.SortFlag = (1 - wt) * 0.1 * c.curTime + wt * temp;
			}
		}	
		Collections.sort(COURIERS);	
	}
*/

	public void sortCouriers(ArrayList<Order> O, double wt){
		double temp = 0.0;
		double alpha = 12;
		Order o = O.get(0);
		for (Courier c : COURIERS)
		{
			temp = c.curTime + c.curAddr.distance(o.From) / 250.0;
			if (o.From.Addr.substring(0,1).equals("A"))
			{
				temp = temp > o.Pickup_time ? (temp - c.curTime + alpha * (temp - o.Pickup_time)) : (temp - c.curTime);
			}
			else
			{
				temp = temp > o.Pickup_time ? (temp - c.curTime + alpha * (temp - o.Pickup_time)) : 
					(o.Pickup_time - c.curTime);
				

			}
			c.SortFlag = (1 - wt) * 0.1 * c.curTime + wt * temp;	
		}	
		Collections.sort(COURIERS);	
	}

	public void sortOrders(String key){
		for (Order o : ORDERS.get(key))
		{
			o.SortFlag = o.Pickup_time;
		}
		Collections.sort(ORDERS.get(key));
	}

	public ArrayList<Order> sortOrders(ArrayList<Order> orders){
		for (Order o : orders)
		{
			o.SortFlag = o.Pickup_time;
		}
		Collections.sort(orders);
		return orders;
	}

	public void deleteOrders(String key, ArrayList<Order> ECS){
		for (Order e : ECS)
			ORDERS.get(key).remove(e);
	}

	public void curOrderNum(){
		ORDER_NUM = 0;
		for (int i = 0; i < KEYS.size(); i++)
		{
			ORDER_NUM += ORDERS.get(KEYS.get(i)).size();
		}
	}

	public void updateCostTime(){
		int T = 0;
		int P = 0;
		for (Courier c : COURIERS)
		{
			T += c.curTime;
			P += getPenalty(c);
		}
		COST_TIME = "基础时长：" + T + "--" + 
			"惩罚时长：" + P + "--" + 
			"总时长：" + (T + P);
	}

	public int getPenalty(Courier c){
		Collections.sort(c.Records);
		int value = 0;
		for (Record r : c.Records)
		{
			if (!r.Amount.substring(0,1).equals("-") && 
				r.Arrival_time > r.Pickup_time)
			{
				value += 5 * (r.Arrival_time - r.Pickup_time);
			}
			if (r.Amount.substring(0,1).equals("-") && 
				r.Arrival_time > r.Delivery_time)
			{
				value += 5 * (r.Arrival_time - r.Delivery_time);
			}
		}
		return value;
	}

//===========================================================================
	public void getData() throws Exception {
		getOutputFile();
		getPoints();
		getOrders();
		getCouriers();
	}

	public void getPoints() throws Exception {
		BufferedReader br = Util.readFile(PATH + "ADDRS.txt");
		String line = br.readLine();
		String[] temp = null;
		while ((line = br.readLine()) != null) {
			temp = line.split(",");
			ADDRS.put(temp[0], new Point(temp[0], Double.parseDouble(temp[1]), Double.parseDouble(temp[2])));
		}
	}

	public void getOrders() throws Exception{
		BufferedReader br = Util.readFile(PATH + "ORDERS.txt");
		String line = br.readLine();
		String[] temp = null;
		String key = null;
		while ((line = br.readLine()) != null) {
			temp = line.split(",");
			Order order = new Order(temp[0], 
				ADDRS.get(temp[1]), 
				ADDRS.get(temp[2]), 
				Integer.parseInt(temp[3]),
				Integer.parseInt(temp[4]),
				Integer.parseInt(temp[5]),
				ADDRS.get(temp[6]));
			key = order.From.Addr;
			if (ORDERS.containsKey(key))
			{
				ORDERS.get(key).add(order);
			}else{
				ArrayList<Order> list = new ArrayList<>();
				list.add(order);
				ORDERS.put(key, list);
				KEYS.add(key);
			}
		}
	}
	
	public void getCouriers() throws Exception{
		BufferedReader br = Util.readFile(PATH + "COURIERS.txt");
		String line = br.readLine();
		while ((line = br.readLine()) != null) {
			COURIERS.add(new Courier(line));
		}
		int K = 0;
		int ct = 0;
		for (int i = 0; i < 124; i++) 
		{
			K = (int)Math.round(ORDERS.get(KEYS.get(i)).size() / 9214.0 * 1003.0);
			if (K == 0)
				continue;
			for (int j = ct; j < ct + K; j++)
			{
				COURIERS.get(j).curAddr = ADDRS.get(KEYS.get(i));
				
			}
			ct += K;
		}
	}

	public void createFolder(String str) {
		boolean success = (new File(str)).mkdir();
		if (success) {
			System.out.println("Directory: " + str + " created");
		} else {
			System.out.println("Directory: " + str + " NOT created");
		}
	}
	public void getOutputFile() throws Exception {
		OUTPUTFILE = "E:/competition/TIANCHI/CAINIAO2/RESULT";	
		createFolder(OUTPUTFILE);
		OUTPUTFILE += "/result.csv";
		ArrayList<Record> header = new ArrayList<>();
		header.add(new Record("Courier_id", "Addr", -1, -1, "Amount", "Order_id", 0, 0));
		Util.writeFile(OUTPUTFILE, header, false);
	}
	public void printToFile() throws Exception{
		if (PRINT)
			for (Courier c : COURIERS)
				Util.writeFile(OUTPUTFILE, c.Records, true);		
	}

}
