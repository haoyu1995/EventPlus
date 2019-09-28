package rpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;

import Entity.Item;
import db.DBConnection;
import db.DBConnectionFactory;

/**
 * Servlet implementation class ItemHistory
 */
@WebServlet("/history")
public class ItemHistory extends HttpServlet {
	/*
	 * Item History is to record the favorite operation of users, including set/unset
	 * */
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ItemHistory() {
        super();
        // TODO Auto-generated constructor stub
    }
    /*{
    	user_id = “1111”,
    	favorite = [
        	“abcd”,
        	“efgh”,
    	]
	  }*/

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		// allow access only if session exists
		HttpSession session = request.getSession(false);
		if (session == null) {
			System.out.println("status403");  
			response.setStatus(403);
			return;
		}
		//get query info from request
		String user_id = request.getParameter("user_id");
		
		JSONArray favoriteItems = new JSONArray();
		
		//connect to DB
		DBConnection conn = DBConnectionFactory.getConnection();
		Set<Item> array = conn.getFavoriteItems(user_id);
		System.out.println(array.size());
		conn.close();
		
		for (Item item : array) {
			JSONObject obj = item.toJSONObject();
			//为了前端代码显示
			try {
				obj.append("favorite", true);
			}catch (Exception e) {
				e.printStackTrace();
			}
			favoriteItems.put(obj);
		}
		
		RpcHelper.writeJsonArray(response, favoriteItems);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		try {
			JSONObject input = RpcHelper.readJsonObject(request);
			String user_id = input.getString("user_id");
			JSONArray array = input.getJSONArray("favorite");
			List<String> itemIds = new ArrayList<>();
			for (int i = 0; i < array.length(); i++) {
				itemIds.add(array.get(i).toString());
			}
			
			DBConnection conn = DBConnectionFactory.getConnection();
			conn.setFavoriteItems(user_id, itemIds);
			conn.close();
			
			RpcHelper.writeJsonObject(response, new JSONObject().put("result", "success!!"));
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		try {
			JSONObject input = RpcHelper.readJsonObject(request);
			String user_id = input.getString("user_id");
			JSONArray array = input.getJSONArray("favorite");
			List<String> itemIds = new ArrayList<>();
			for (int i = 0; i < array.length(); i++) {
				itemIds.add(array.get(i).toString());
			}
			
			DBConnection conn = DBConnectionFactory.getConnection();
			conn.unsetFavoriteItems(user_id, itemIds);
			conn.close();
			
			RpcHelper.writeJsonObject(response, new JSONObject().put("result", "success!!"));
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}

}
