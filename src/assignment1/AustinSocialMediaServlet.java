package assignment1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class AustinSocialMediaServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static List<Department> departmentList = null;
	private static List<String> typeSet = new ArrayList<String>();
	private static int requestNo = 0;
	private static String session = "";
	private static String username = "";
	private static String account_name = "";
	private static String type = "";

	protected static void retrieveTypes() {

		Iterator<Department> iter = departmentList.listIterator();

		while (iter.hasNext()) {
			typeSet.add(iter.next().getType());
		}
	}

	protected static void fetchData() {
		// getting the data from the url
		URL url = null;

		try {
			url = new URL("https://www.cs.utexas.edu/~devdatta/ej42-f7za.json");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		try {
			URLConnection connection = url.openConnection();

			InputStream input = connection.getInputStream();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buf = new byte[8192];
			int len = 0;
			while ((len = input.read(buf)) != -1) {
				baos.write(buf, 0, len);
			}
			String body = new String(baos.toByteArray(), "UTF-8");
			deserializeJson(body);
			retrieveTypes();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// converts json to java objects - called inside fetch data
	private static void deserializeJson(String json) {
		final Gson gson = new Gson();

		final Type deptListType = new TypeToken<List<Department>>() {
		}.getType();
		departmentList = gson.fromJson(json, deptListType);
	}

	// iterates over set of departments and attempts to find match to request
	private static String queryDepartments(String account_name, String type) {

		Iterator<Department> iter = departmentList.listIterator();
		Department dept;

		while (iter.hasNext()) {
			dept = iter.next();
			try {
				if (account_name.equalsIgnoreCase(dept.getAccount()) && type.equalsIgnoreCase(dept.getType())) {
					return dept.getAccount() + " " + dept.getType() + " " + dept.getExternalLink();
				}
			} catch (NullPointerException e) {
				return "No URL available";
			}

		}
		return "Nothing Found";
	}

	// queries departments if no type is provided from request
	private static String queryDepartmentsWithoutType(String account_name) {
		String urls = "";

		Iterator<Department> iter = departmentList.listIterator();
		Department dept;

		while (iter.hasNext()) {
			dept = iter.next();

			if (account_name.equalsIgnoreCase(dept.getAccount())) {
				String tempURL = "";

				try {
					tempURL = dept.getExternalLink();
				} catch (NullPointerException e) {
					tempURL = "NO URL AVAILABLE";
				}
				urls += dept.getAccount() + " " + dept.getType() + " " + tempURL + "<br/>\r\n";
			}

		}

		return urls;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");

		PrintWriter writer = response.getWriter();

		fetchData();
		String resultDeptData = "";

		if (request.getParameter("session") != null) {
			session = request.getParameter("session");
		}

		if (request.getParameter("username") != null) {
			username = request.getParameter("username");
		}

		account_name = request.getParameter("account_name");

		type = request.getParameter("type");

		if (account_name == null)
			account_name = "";

		if (type == null)
			type = "";

		if (!session.equals("") && !username.equals("")) {
			Cookie username_cookie = new Cookie("username", username);
			Cookie session_cookie = new Cookie("session", session);
			username_cookie.setMaxAge(10 * 60);
			response.addCookie(session_cookie);
			response.addCookie(username_cookie);
		}

		Cookie[] cks = request.getCookies();

		// end session, delete all cookies and reset username
		if (!session.equals("") && cks != null) {
			if (session.equalsIgnoreCase("end")) {
				for (Cookie c : cks) {
					c.setMaxAge(0);
					c.setValue(null);
					response.addCookie(c);
				}
				username = "";
			}
		}

		// creates cookies containing requests if session exists with a username
		if (!account_name.equals("") && session.equalsIgnoreCase("start") && !username.equals("")) {
			response.addCookie(new Cookie("request" + requestNo,
					request.getRequestURL().toString() + "?" + request.getQueryString()));
			requestNo += 1;
		}

		if (!account_name.equals("") && !type.equals("")) {
			if (typeSet.contains(type.toUpperCase())) {
				resultDeptData = queryDepartments(account_name, type);
			} else {
				writer.println("Disallowed value for specified for paramater TYPE");
			}
		} else if (!account_name.equals("") && type.equals("")) {
			resultDeptData = queryDepartmentsWithoutType(account_name);
		}

		// printing of results to browser
		writer.println("Visited URL's<br/>\r\n");

		for (int i = 0; cks != null && i < cks.length; i++) {
			Cookie tempCookie = cks[i];

			if (!tempCookie.getName().equals("session") && !tempCookie.getName().equals("username")) {

				String reqVal = tempCookie.getValue();
				if (reqVal != null) {
					writer.append(reqVal).append("<br/>\r\n");
				}
			}
		}

		writer.println("URL Data<br/>\r\n");

		if (account_name.length() != 0) {
			writer.append(resultDeptData);
		}
	}
}